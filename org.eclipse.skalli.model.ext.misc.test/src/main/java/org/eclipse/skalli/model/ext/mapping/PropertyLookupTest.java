package org.eclipse.skalli.model.ext.mapping;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.skalli.ext.mapping.PropertyLookup;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestExtensionService;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class PropertyLookupTest {

    private static class TestPropertyLookup extends PropertyLookup {

        public TestPropertyLookup(EntityBase entity) {
            super(entity);
        }

        public TestPropertyLookup(EntityBase entity, Map<String, Object> customProperties) {
            super(entity, customProperties);
        }

        @Override
        protected ExtensionService<?> getExtensionService(ExtensionEntityBase extension) {
            return new TestExtensionService();
        }
    }

    @Test
    public void testLookUp() throws Exception {
        Project project = createProject();
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("userId", "hugo");
        TestPropertyLookup lookup = new TestPropertyLookup(project, props);
        Assert.assertEquals("bla.blubb", lookup.lookup(Project.PROPERTY_PROJECTID));
        Assert.assertEquals("Blubber", lookup.lookup(Project.PROPERTY_NAME));
        Assert.assertEquals("foobar", lookup.lookup("testext." + TestExtension.PROPERTY_STR));
        Assert.assertEquals("a,b,c", lookup.lookup("testext." + TestExtension.PROPERTY_ITEMS));
        Assert.assertEquals("hugo", lookup.lookup("userId"));
        Assert.assertNull(lookup.lookup(Project.PROPERTY_DESCRIPTION));
        Assert.assertNull(lookup.lookup("testext.abc"));
        Assert.assertNull(lookup.lookup(null));
        Assert.assertNull(lookup.lookup(""));
    }

    @Test
    public void testLookUpNoProject() throws Exception {
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("userId", "hugo");
        TestPropertyLookup lookup = new TestPropertyLookup(null, props);
        Assert.assertEquals("hugo", lookup.lookup("userId"));
        Assert.assertNull(lookup.lookup(Project.PROPERTY_PROJECTID));
        Assert.assertNull(lookup.lookup("testext.abc"));
        Assert.assertNull(lookup.lookup(null));
        Assert.assertNull(lookup.lookup(""));
    }

    @Test
    public void testLookUpNoCustomProps() throws Exception {
        Project project = createProject();
        TestPropertyLookup lookup = new TestPropertyLookup(project);
        Assert.assertNull(lookup.lookup("userId"));
        Assert.assertEquals("bla.blubb", lookup.lookup(Project.PROPERTY_PROJECTID));
        Assert.assertNull(lookup.lookup("testext.abc"));
        Assert.assertNull(lookup.lookup(null));
        Assert.assertNull(lookup.lookup(""));
    }

    private Project createProject() {
        Project project = new Project("bla.blubb", null, "Blubber");
        TestExtension ext = new TestExtension();
        ext.setStr("foobar");
        ext.addItem("a");
        ext.addItem("b");
        ext.addItem("c");
        project.addExtension(ext);
        return project;
    }
}
