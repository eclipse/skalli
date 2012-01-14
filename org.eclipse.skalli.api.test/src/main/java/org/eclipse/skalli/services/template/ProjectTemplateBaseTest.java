package org.eclipse.skalli.services.template;

import org.eclipse.skalli.model.ProjectNature;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("nls")
public class ProjectTemplateBaseTest {

    private static final String PROPERTY_ID = "prop";
    private static final String PROPERTY_ID1 = "prop1";
    private static final String PROPERTY_ID2 = "prop2";
    private static final String UNKNOWN_PROPERTY_ID = "unknownprop";
    private static final String EXTENSION_NAME = "FoobarExtension";
    private static final String EXTENSION_NAME1 = "FoobarExtension1";
    private static final String UNKNOWN_EXTENSION_NAME = "UnknownExtension";
    private static final String ID1 = "test-project";
    private static final String CAPTION1 = "Test Project Template";
    private static final String DESCRPIPTION1 = "This is a project template for testing";

    private static final String ID2 = "test-component";
    private static final String CAPTION2 = "Test Component Template";
    private static final String DESCRPIPTION2 = "This is a component template for testing";


    private static class TestProjectTemplate extends ProjectTemplateBase {

        @Override
        public String getId() {
            return ID1;
        }

        @Override
        public String getDisplayName() {
            return CAPTION1;
        }

        @Override
        public String getDescription() {
            return DESCRPIPTION1;
        }

        @Override
        public float getRank() {
            return 4711.0f;
        }

        @Override
        public ProjectNature getProjectNature() {
            return ProjectNature.PROJECT;
        }
    }

    private static class TestComponentTemplate extends ProjectTemplateBase {

        @Override
        public String getId() {
            return ID2;
        }

        @Override
        public String getDisplayName() {
            return CAPTION2;
        }

        @Override
        public String getDescription() {
            return DESCRPIPTION2;
        }

        @Override
        public float getRank() {
            return 815.0f;
        }

        @Override
        public ProjectNature getProjectNature() {
            return ProjectNature.COMPONENT;
        }
    }

    @Test
    public void testInitial() throws Exception {
        TestProjectTemplate projectTemplate = new TestProjectTemplate();
        TestComponentTemplate componentTemplate = new TestComponentTemplate();
        Assert.assertEquals(ID1, projectTemplate.getId());
        Assert.assertEquals(CAPTION1, projectTemplate.getDisplayName());
        Assert.assertEquals(DESCRPIPTION1, projectTemplate.getDescription());
        Assert.assertNull(projectTemplate.getIncludedExtensions());
        Assert.assertNull(projectTemplate.getExcludedExtensions());
        Assert.assertTrue(projectTemplate.isAllowedSubprojectTemplate(projectTemplate));
        Assert.assertTrue(componentTemplate.isAllowedSubprojectTemplate(componentTemplate));
        Assert.assertTrue(projectTemplate.isAllowedSubprojectTemplate(componentTemplate));
        Assert.assertFalse(componentTemplate.isAllowedSubprojectTemplate(projectTemplate));
        Assert.assertFalse(projectTemplate.isEnabled(EXTENSION_NAME));
        Assert.assertFalse(projectTemplate.isVisible(EXTENSION_NAME));
        Assert.assertTrue(-1.0f == projectTemplate.getRank(EXTENSION_NAME));
    }

    @Test
    public void testValueNullDefault() throws Exception {
        TestProjectTemplate projectTemplate = new TestProjectTemplate();
        Assert.assertNull(projectTemplate.getCaption(EXTENSION_NAME, PROPERTY_ID));
        projectTemplate.setCaption(EXTENSION_NAME, PROPERTY_ID, "foobar");
        Assert.assertEquals("foobar", projectTemplate.getCaption(EXTENSION_NAME, PROPERTY_ID));
        Assert.assertNull(projectTemplate.getCaption(UNKNOWN_EXTENSION_NAME, PROPERTY_ID));
        Assert.assertNull(projectTemplate.getCaption(EXTENSION_NAME, UNKNOWN_PROPERTY_ID));
        projectTemplate.setCaption(EXTENSION_NAME, PROPERTY_ID1, "foobar1");
        Assert.assertEquals("foobar1", projectTemplate.getCaption(EXTENSION_NAME, PROPERTY_ID1));
        projectTemplate.setCaption(EXTENSION_NAME1, PROPERTY_ID2, "foobar2");
        Assert.assertEquals("foobar2", projectTemplate.getCaption(EXTENSION_NAME1, PROPERTY_ID2));
        projectTemplate.setCaption(EXTENSION_NAME, PROPERTY_ID, null);
        Assert.assertNull(projectTemplate.getCaption(EXTENSION_NAME, PROPERTY_ID));
        Assert.assertEquals("foobar1", projectTemplate.getCaption(EXTENSION_NAME, PROPERTY_ID1));
    }

    @Test
    public void testValueWithDefault() throws Exception {
        TestProjectTemplate projectTemplate = new TestProjectTemplate();
        Assert.assertEquals(Integer.MAX_VALUE, projectTemplate.getMaxSize(EXTENSION_NAME, PROPERTY_ID));
        projectTemplate.setMaxSize(EXTENSION_NAME, PROPERTY_ID, 123);
        Assert.assertEquals(123, projectTemplate.getMaxSize(EXTENSION_NAME, PROPERTY_ID));
        Assert.assertEquals(Integer.MAX_VALUE, projectTemplate.getMaxSize(UNKNOWN_EXTENSION_NAME, PROPERTY_ID));
        Assert.assertEquals(Integer.MAX_VALUE, projectTemplate.getMaxSize(EXTENSION_NAME, UNKNOWN_PROPERTY_ID));
        projectTemplate.setMaxSize(EXTENSION_NAME, PROPERTY_ID1, 456);
        Assert.assertEquals(456, projectTemplate.getMaxSize(EXTENSION_NAME, PROPERTY_ID1));
        projectTemplate.setMaxSize(EXTENSION_NAME, PROPERTY_ID2, 789);
        Assert.assertEquals(789, projectTemplate.getMaxSize(EXTENSION_NAME, PROPERTY_ID2));
        projectTemplate.setMaxSize(EXTENSION_NAME, PROPERTY_ID2, 789);
    }

    @Test
    public void testContains() throws Exception {
        TestProjectTemplate projectTemplate = new TestProjectTemplate();
        Assert.assertFalse(projectTemplate.isNewItemsAllowed(EXTENSION_NAME, PROPERTY_ID));
        projectTemplate.setNewItemsAllowed(EXTENSION_NAME, PROPERTY_ID, true);
        Assert.assertTrue(projectTemplate.isNewItemsAllowed(EXTENSION_NAME, PROPERTY_ID));
        Assert.assertFalse(projectTemplate.isNewItemsAllowed(UNKNOWN_EXTENSION_NAME, PROPERTY_ID));
        Assert.assertFalse(projectTemplate.isNewItemsAllowed(EXTENSION_NAME, UNKNOWN_PROPERTY_ID));
        projectTemplate.setNewItemsAllowed(EXTENSION_NAME, PROPERTY_ID1, true);
        Assert.assertTrue(projectTemplate.isNewItemsAllowed(EXTENSION_NAME, PROPERTY_ID1));
        projectTemplate.setNewItemsAllowed(EXTENSION_NAME, PROPERTY_ID2, true);
        Assert.assertTrue(projectTemplate.isNewItemsAllowed(EXTENSION_NAME, PROPERTY_ID2));
        projectTemplate.setNewItemsAllowed(EXTENSION_NAME1, PROPERTY_ID2, true);
        Assert.assertTrue(projectTemplate.isNewItemsAllowed(EXTENSION_NAME1, PROPERTY_ID2));
        projectTemplate.setNewItemsAllowed(EXTENSION_NAME1, PROPERTY_ID2, false);
        Assert.assertFalse(projectTemplate.isNewItemsAllowed(EXTENSION_NAME1, PROPERTY_ID2));
        projectTemplate.setNewItemsAllowed(UNKNOWN_EXTENSION_NAME, PROPERTY_ID, false);
        Assert.assertFalse(projectTemplate.isNewItemsAllowed(UNKNOWN_EXTENSION_NAME, PROPERTY_ID));
        projectTemplate.setNewItemsAllowed(EXTENSION_NAME, UNKNOWN_PROPERTY_ID, false);
        Assert.assertFalse(projectTemplate.isNewItemsAllowed(EXTENSION_NAME, UNKNOWN_PROPERTY_ID));
    }

    @Test
    public void testBooleanValue() throws Exception {

    }

}
