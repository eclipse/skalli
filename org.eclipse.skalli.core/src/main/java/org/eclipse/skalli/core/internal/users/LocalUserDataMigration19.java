package org.eclipse.skalli.core.internal.users;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.extension.DataMigrationBase;
import org.eclipse.skalli.services.extension.MigrationException;
import org.eclipse.skalli.services.extension.MigrationUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class LocalUserDataMigration19 extends DataMigrationBase {

    public LocalUserDataMigration19() {
        super(User.class, 19);
    }

    /**
     * Changes from model version 0->1:
     * <ol>
     *   <li>renamed root element org.eclipse.skalli.common.User to "user"</li>
     * </ol>
     */
    @SuppressWarnings("nls")
    @Override
    public void migrate(Document doc) throws MigrationException {
        MigrationUtils.renameTag(doc, doc.getDocumentElement(), "entity-user");
        Element elem = MigrationUtils.getElementOfEntity(doc, "uuid");
        if (elem == null) {
            elem = doc.createElement("uuid");
            elem.setTextContent(UUID.randomUUID().toString());
            doc.getDocumentElement().insertBefore(elem, doc.getDocumentElement().getFirstChild());
        }
    }

    // before the "grand" refactoring, User was in package org.eclipse.skalli.common!
    @SuppressWarnings("nls")
    @Override
    public boolean handlesType(String entityClassName) {
        return super.handlesType(entityClassName) ||
                StringUtils.equals(entityClassName, "org.eclipse.skalli.common.User");
    }

}
