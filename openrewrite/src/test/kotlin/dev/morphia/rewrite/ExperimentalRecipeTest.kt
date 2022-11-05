package dev.morphia.rewrite

import org.openrewrite.java.Assertions.java
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.testng.annotations.Test

class ExperimentalRecipeTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(ExperimentalRecipe())
    }

    @Test
    fun addsHelloToA() = rewriteRun(
        java(
            """
                package com.foo.bar;
                
                import dev.morphia.aggregation.experimental.expressions.Filter;
    
                class A {
                }
            """,
            """
                package com.foo.bar;
                
                import dev.morphia.aggregation.experimental.expressions.Filter;
    
                class A {
                }
            """
        )
    )

    @Test
    fun doesNotChangeExistingHello() = rewriteRun(
        java(
            """
                package com.yourorg;
    
                class A {
                    public String hello() { return ""; }
                }
            """
        )
    )

    @Test
    fun doesNotChangeOtherClass() = rewriteRun(
        java(
            """
                package com.yourorg;
    
                class B {
                }
            """
        )
    )
}