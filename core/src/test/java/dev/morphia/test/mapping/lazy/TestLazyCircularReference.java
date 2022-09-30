package dev.morphia.test.mapping.lazy;

import dev.morphia.Datastore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import dev.morphia.test.mapping.ProxyTestBase;
import dev.morphia.test.models.TestEntity;
import org.bson.types.ObjectId;
import org.testng.annotations.Test;

import java.util.List;

import static dev.morphia.query.filters.Filters.eq;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

@Test(groups = "references")
public class TestLazyCircularReference extends ProxyTestBase {

    public void testCircularReferences() {
        getMapper().map(Bob.class, Alice.class);
        var bob = new Bob();
        var alice = new Alice();
        getDs().save(List.of(bob, alice));

        bob.alice = alice;
        alice.bob = bob;
        getDs().save(List.of(bob, alice));

        Bob loadedBob = getDs().find(Bob.class)
                .filter(eq("_id", bob.id))
                .first();
        Alice loadedAlice = getDs().find(Alice.class)
                .filter(eq("_id", alice.id))
                .first();

        assertEquals(bob.name, alice.bob.name);
        loadedBob.name = "loaded";
        assertEquals(bob.name, alice.bob.name);

        assertEquals(bob.alice.name, bob.alice.bob.alice.name);
        bob.alice.name = "alice loaded";
        assertEquals(bob.alice.name, bob.alice.bob.alice.name);

        assertNotEquals(bob.alice.name, loadedAlice.name);
    }

    @Entity
    public static class Bob {
        @Id
        private ObjectId id;
        @Reference(lazy = true)
        private Alice alice;
        private String name;
    }

    @Entity
    public static class Alice {
        @Id
        private ObjectId id;
        @Reference(lazy = true)
        private Bob bob;
        private String name;
    }

    public void testGetKeyWithoutFetching() {
        checkForProxyTypes();

        RootEntity root = new RootEntity();
        getDs().save(root);

        final ReferencedEntity reference = new ReferencedEntity();
        reference.parent = root;

        root.reference = reference;
        reference.setFoo("bar");

        final ObjectId id = getDs().save(reference).getId();
        getDs().save(root);

        final Datastore datastore = getDs();
        root = datastore.find(RootEntity.class)
                .filter(eq("_id", root.getId()))
                .first();

        final ReferencedEntity referenced = root.reference;

        assertIsProxy(referenced);
        assertNotFetched(referenced);
        assertEquals(referenced.getId(), id);
        assertNotFetched(referenced);
        referenced.getFoo();
        assertFetched(referenced);
    }

    public static class RootEntity extends TestEntity {
        @Reference(lazy = true)
        private ReferencedEntity reference;
        @Reference(lazy = true)
        private ReferencedEntity secondReference;

        public ReferencedEntity getReference() {
            return reference;
        }

        public void setReference(ReferencedEntity reference) {
            this.reference = reference;
        }

        public ReferencedEntity getSecondReference() {
            return secondReference;
        }

        public void setSecondReference(ReferencedEntity secondReference) {
            this.secondReference = secondReference;
        }
    }

    public static class ReferencedEntity extends TestEntity {
        private String foo;

        @Reference(lazy = true)
        private RootEntity parent;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String string) {
            foo = string;
        }

        public RootEntity getParent() {
            return parent;
        }

        public void setParent(RootEntity parent) {
            this.parent = parent;
        }
    }
}
