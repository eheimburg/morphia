package dev.morphia.rewrite

import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit.SECONDS
import org.slf4j.LoggerFactory
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream

class ExperimentalRecipeTest {
    companion object {
        val logger = LoggerFactory.getLogger(ExperimentalRecipeTest::class.java)
        private val SOFIA = "target/generated-sources/sofia/dev/morphia/sofia/Sofia.java"
    }

    val gitDir = File("target", "morphia-2.2.x")
    val workspace = File("target", "tests-2.2.x")
    val core = File(gitDir, "core")

    @Test
    fun updatePackages() {
        cloneRepo()
        prepareWorkspace()
        var result =
            execute(
                "mvn org.openrewrite.maven:rewrite-maven-plugin:runNoFork " +
                    "-Drewrite.configLocation=${rewrite()} " +
                    "-Drewrite.activeRecipes=dev.morphia.experimental",
                workspace
            )
        assertEquals(result.exitValue, 0)

        //        result = execute("mvn test-compile", workspace)
        //        assertEquals(result, 0)

        /*
        - org.openrewrite.java.ChangePackage:
            oldPackageName: dev.morphia.query.experimental
            newPackageName: dev.morphia.query
            recursive: true
        - org.openrewrite.java.ChangePackage:
            oldPackageName: dev.morphia.aggregation.experimental
            newPackageName: dev.morphia.aggregation
            recursive: true
        - org.openrewrite.java.ChangePackage:
            oldPackageName: dev.morphia.query.experimental.updates
            newPackageName: dev.morphia.query.updates
            recursive: true

                       */
    }

    fun rewrite(): File {
        var current = File(".").canonicalFile
        while (!File(current, "rewrite.yml").exists()) {
            current = current.parentFile
        }

        return File(current, "rewrite.yml")
    }

    private fun prepareWorkspace() {
        if (workspace.exists()) {
            workspace.deleteRecursively()
        }

        workspace.mkdirs()
        val stream = javaClass.getResourceAsStream("/test-pom.xml")
        stream.transferTo(FileOutputStream(File(workspace, "pom.xml")))
        val test = File(workspace, "src/test/java")
        test.mkdirs()
        File(core, "src/test/java").copyRecursively(test)
    }

    private fun cloneRepo() {
        if (!gitDir.exists()) {
            logger.info("$gitDir doesn't exist.  Cloning repository.")
            execute("git clone -b 2.2.x https://github.com/MorphiaOrg/morphia/ $gitDir")
            //        } else {
            //            logger.info("$gitDir exists.  Resetting.")
            //            execute("git reset --hard", gitDir)
        }
    }
    private fun execute(command: String, cwd: File? = null): ProcessResult {
        File("target/surefire-reports").deleteRecursively()
        logger.info(command)
        val processResult =
            ProcessExecutor()
                .environment(HashMap(System.getenv()))
                .directory(cwd)
                .commandSplit(command)
                .redirectOutput(Slf4jStream.of(logger).asInfo())
                .redirectError(Slf4jStream.of(logger).asError())
                .readOutput(true)
                .destroyOnExit()
                .start()

        return processResult.future.get(300, SECONDS)
    }
}
