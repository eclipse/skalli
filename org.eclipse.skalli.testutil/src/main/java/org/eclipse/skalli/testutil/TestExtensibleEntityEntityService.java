package org.eclipse.skalli.testutil;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityServiceBase;

public class TestExtensibleEntityEntityService extends EntityServiceBase<TestExtensibleEntityBase> {

    private int modelVersion;

    public TestExtensibleEntityEntityService(int modelVersion) {
        this.modelVersion = modelVersion;
    }

    @Override
    public Class<TestExtensibleEntityBase> getEntityClass() {
        return TestExtensibleEntityBase.class;
    }

    @Override
    public int getModelVersion() {
        return modelVersion;
    }

    @Override
    protected void validateEntity(TestExtensibleEntityBase entity) throws ValidationException {
    }

    @Override
    protected SortedSet<Issue> validateEntity(TestExtensibleEntityBase entity, Severity minSeverity) {
        return new TreeSet<Issue>();
    }

}
