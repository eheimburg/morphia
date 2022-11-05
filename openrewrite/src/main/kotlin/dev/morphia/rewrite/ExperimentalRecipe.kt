package dev.morphia.rewrite

import org.openrewrite.Cursor
import org.openrewrite.ExecutionContext
import org.openrewrite.Recipe
import org.openrewrite.SourceFile
import org.openrewrite.TreeVisitor
import org.openrewrite.internal.ListUtils
import org.openrewrite.java.JavaIsoVisitor
import org.openrewrite.java.JavaTemplate
import org.openrewrite.java.JavaVisitor
import org.openrewrite.java.RemoveImport
import org.openrewrite.java.search.UsesType
import org.openrewrite.java.tree.J
import org.openrewrite.java.tree.J.ClassDeclaration
import org.openrewrite.java.tree.J.CompilationUnit
import org.openrewrite.java.tree.J.FieldAccess
import org.openrewrite.java.tree.J.Import
import org.openrewrite.java.tree.J.MethodDeclaration
import org.openrewrite.java.tree.J.MethodInvocation
import org.openrewrite.java.tree.J.NewClass
import org.openrewrite.java.tree.J.Package
import org.openrewrite.java.tree.JavaSourceFile
import org.openrewrite.java.tree.JavaType
import org.openrewrite.java.tree.JavaType.Array
import org.openrewrite.java.tree.JavaType.Class
import org.openrewrite.java.tree.JavaType.FullyQualified
import org.openrewrite.java.tree.JavaType.GenericTypeVariable
import org.openrewrite.java.tree.JavaType.Method
import org.openrewrite.java.tree.JavaType.Parameterized
import org.openrewrite.java.tree.JavaType.ShallowClass
import org.openrewrite.java.tree.JavaType.Unknown
import org.openrewrite.java.tree.JavaType.Variable
import org.openrewrite.java.tree.Space
import org.openrewrite.java.tree.TypeUtils
import org.openrewrite.java.tree.TypedTree
import org.openrewrite.marker.SearchResult
import java.nio.file.Paths

class ExperimentalRecipe : Recipe() {
    override fun getDisplayName() = "Promote Experimental"

    override fun getDescription() =
        "Moves classes out of the experimental packages as indicated in issues #1859, #1863, #1865, #1866"

    override fun getVisitor(): TreeVisitor<*, ExecutionContext> {
        return ExperimentalVisitor()
    }

    override fun getSingleSourceApplicableTest(): JavaVisitor<ExecutionContext> {
        return object : JavaIsoVisitor<ExecutionContext>() {
            override fun visitJavaSourceFile(cu: JavaSourceFile, executionContext: ExecutionContext): JavaSourceFile {
                val packageDeclaration = cu.packageDeclaration
                if (packageDeclaration != null) {
                    val original = packageDeclaration.expression
                        .printTrimmed(cursor).replace("\\s".toRegex(), "")
                    if (original.contains(".experimental.")) {
                        return SearchResult.found(cu)
                    }
                }

                doAfterVisit(UsesType(".*\\.experimental\\..*"))
                return cu
            }
        }
    }
}

private class ExperimentalVisitor : JavaIsoVisitor<ExecutionContext>() {
    private val oldNameToChangedType: MutableMap<String, JavaType?> = HashMap()
//    private val newPackageType: Class = ShallowClass.build(newPackageName)
    override fun visitJavaSourceFile(cu: JavaSourceFile, ctx: ExecutionContext): JavaSourceFile {
        var c = super.visitJavaSourceFile(cu, ctx)
        val changingTo = cursor.getMessage<String>(RENAME_TO_KEY)
        if (changingTo != null) {
            val path: String = (c as SourceFile).getSourcePath().toString().replace('\\', '/')
            val changingFrom = cursor.getMessage<String>(RENAME_FROM_KEY)!!
            c = (c as SourceFile).withSourcePath(
                Paths.get(
                    path.replaceFirst(
                        changingFrom.replace('.', '/').toRegex(),
                        changingTo.replace('.', '/')
                    )
                )
            )
            for (anImport in c.imports) {
                if (anImport.packageName == changingTo) {
                    c = RemoveImport<ExecutionContext>(anImport.typeName, true).visitJavaSourceFile(c, ctx)
                }
            }
        }
        return c
    }

    override fun visitFieldAccess(fieldAccess: FieldAccess, ctx: ExecutionContext): FieldAccess {
        var f = super.visitFieldAccess(fieldAccess, ctx)
        if (f.isFullyQualifiedClassReference(oldPackageName)) {
            val parent: Cursor? = cursor.parent
            if (parent != null &&  // Ensure the parent isn't a J.FieldAccess OR the parent doesn't match the target package name.
                (parent.getValue() !is FieldAccess ||
                    !(parent.getValue() as FieldAccess).isFullyQualifiedClassReference(newPackageName))
            ) {
                f = TypeTree.build((newPackageType as FullyQualified).fullyQualifiedName)
                    .withPrefix(f.prefix)
            }
        }
        return f
    }

    override fun visitPackage(pkg: Package, context: ExecutionContext): Package {
        var pkg = pkg
        val original = pkg.expression.printTrimmed(cursor).replace("\\s".toRegex(), "")
        cursor.putMessageOnFirstEnclosing(CompilationUnit::class.java, RENAME_FROM_KEY, original)
        if (original.contains(".experimental.")) {
            cursor.putMessageOnFirstEnclosing(CompilationUnit::class.java, RENAME_TO_KEY, newPackageName)
            if (!newPackageName.isEmpty()) {
                pkg = pkg.withTemplate(JavaTemplate.builder({ cursor }, newPackageName).build(), pkg.coordinates.replace())
            } else {
                // Covers unlikely scenario where the package is removed.
                cursor.putMessageOnFirstEnclosing(CompilationUnit::class.java, "UPDATE_PREFIX", true)
                pkg = null
            }
        } else if (isTargetRecursivePackageName(original)) {
            val changingTo = getNewPackageName(original)
            cursor.putMessageOnFirstEnclosing(CompilationUnit::class.java, RENAME_TO_KEY, changingTo)
            pkg = pkg.withTemplate(JavaTemplate.builder({ cursor }, changingTo).build(), pkg.coordinates.replace())
        }
        return pkg
    }

    override fun visitImport(_import: Import, executionContext: ExecutionContext): Import {
        // Polls message before calling super to change the prefix of the first import if applicable.
        var _import = _import
        val updatePrefix = cursor.pollNearestMessage<Boolean>("UPDATE_PREFIX")
        if (updatePrefix != null && updatePrefix) {
            _import = _import.withPrefix(Space.EMPTY)
        }
        return super.visitImport(_import, executionContext)
    }

    override fun visitClassDeclaration(classDecl: ClassDeclaration, ctx: ExecutionContext): ClassDeclaration {
        var c = super.visitClassDeclaration(classDecl, ctx)
        val updatePrefix = cursor.pollNearestMessage<Boolean>("UPDATE_PREFIX")
        if (updatePrefix != null && updatePrefix) {
            c = c.withPrefix(Space.EMPTY)
        }
        return c
    }

    @Nullable
    override fun visitType(@Nullable javaType: JavaType, executionContext: ExecutionContext): JavaType {
        return updateType(javaType)
    }

    override fun postVisit(tree: J, executionContext: ExecutionContext): J {
        val j = super.postVisit(tree, executionContext)
        if (j is MethodDeclaration) {
            val m = j
            return m.withMethodType(updateType(m.methodType))
        } else if (j is MethodInvocation) {
            val m = j
            return m.withMethodType(updateType(m.methodType))
        } else if (j is NewClass) {
            val n = j
            return n.withConstructorType(updateType(n.constructorType))
        } else if (j is TypedTree) {
            return j.withType(updateType(j.type))
        }
        return j
    }

    @Nullable
    private fun updateType(@Nullable oldType: JavaType): JavaType {
        if (oldType == null || oldType is Unknown) {
            return oldType
        }
        val type = oldNameToChangedType[oldType.toString()]
        if (type != null) {
            return type
        }
        if (oldType is Parameterized) {
            var pt = oldType
            pt = pt.withTypeParameters(ListUtils.map(pt.typeParameters) { tp ->
                if (tp is FullyQualified) {
                    val tpFq = tp as FullyQualified
                    if (isTargetFullyQualifiedType(tpFq)) {
                        return@map TypeUtils.asFullyQualified(JavaType.buildType(getNewPackageName(tpFq.packageName) + "." + tpFq.className))
                    }
                }
                tp
            })
            if (isTargetFullyQualifiedType(pt)) {
                pt = pt.withType(updateType(pt.type) as FullyQualified)
            }
            oldNameToChangedType[oldType.toString()] = pt
            return pt
        } else if (oldType is FullyQualified) {
            val original: FullyQualified = TypeUtils.asFullyQualified(oldType)
            if (isTargetFullyQualifiedType(original)) {
                val fq: FullyQualified =
                    TypeUtils.asFullyQualified(JavaType.buildType(getNewPackageName(original.packageName) + "." + original.className))
                oldNameToChangedType[oldType.toString()] = fq
                return fq
            }
        } else if (oldType is GenericTypeVariable) {
            var gtv = oldType
            gtv = gtv.withBounds(ListUtils.map(gtv.bounds) { b ->
                if (b is FullyQualified && isTargetFullyQualifiedType(b as FullyQualified)) {
                    return@map updateType(b)
                }
                b
            })
            oldNameToChangedType[oldType.toString()] = gtv
            return gtv
        } else if (oldType is Variable) {
            var variable = oldType
            variable = variable.withType(updateType(variable.type))
            oldNameToChangedType[oldType.toString()] = variable
            return variable
        } else if (oldType is Array) {
            var array = oldType
            array = array.withElemType(updateType(array.elemType))
            oldNameToChangedType[oldType.toString()] = array
            return array
        }
        return oldType
    }

    @Nullable
    private fun updateType(@Nullable oldMethodType: Method?): Method? {
        if (oldMethodType != null) {
            var method = oldNameToChangedType[oldMethodType.toString()] as Method?
            if (method != null) {
                return method
            }
            method = oldMethodType
            method = method.withDeclaringType(updateType(method.declaringType) as FullyQualified)
                .withReturnType(updateType(method.returnType))
                .withParameterTypes(ListUtils.map(method.parameterTypes, this::updateType))
            oldNameToChangedType[oldMethodType.toString()] = method
            return method
        }
        return null
    }

    private fun getNewPackageName(packageName: String): String {
        return if ((recursive == null || recursive) && !newPackageName.endsWith(packageName.substring(oldPackageName.length()))) newPackageName + packageName.substring(
            oldPackageName.length()
        ) else newPackageName
    }

    private fun isTargetFullyQualifiedType(@Nullable fq: FullyQualified?): Boolean {
        return fq != null &&
            (fq.packageName == oldPackageName && !fq.className.isEmpty() ||
                isTargetRecursivePackageName(fq.packageName))
    }

    private fun isTargetRecursivePackageName(packageName: String): Boolean {
        return (recursive == null || recursive) && packageName.startsWith(oldPackageName) && !packageName.startsWith(newPackageName)
    }

    companion object {
        private const val RENAME_TO_KEY = "renameTo"
        private const val RENAME_FROM_KEY = "renameFrom"
    }
}

