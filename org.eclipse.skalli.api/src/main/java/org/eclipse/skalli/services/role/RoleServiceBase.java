package org.eclipse.skalli.services.role;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.Project;

public abstract class RoleServiceBase implements RoleService {

    @Override
    public SortedSet<Member> getMembers(Project project, String... roles) {
        TreeSet<Member> result = new TreeSet<Member>();
        if (roles == null || roles.length == 0) {
            return result;
        }
        Map<String, SortedSet<Member>> members = getMembersByRole(project);
        if (members.isEmpty()) {
            return result;
        }
        for (String role: roles) {
            Set<Member> membersWithRole = members.get(role);
            if (membersWithRole != null) {
                result.addAll(membersWithRole);
            }
        }
        return result;
    }

}
