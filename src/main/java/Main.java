package main.java;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.dfa.axis.factory.DfaServices;
import com.google.api.ads.dfa.axis.v1_20.ActiveFilter;
import com.google.api.ads.dfa.axis.v1_20.SortOrder;
import com.google.api.ads.dfa.axis.v1_20.UserRecordSet;
import com.google.api.ads.dfa.axis.v1_20.UserRemote;
import com.google.api.ads.dfa.axis.v1_20.UserRole;
import com.google.api.ads.dfa.axis.v1_20.UserRoleRecordSet;
import com.google.api.ads.dfa.axis.v1_20.UserRoleRemote;
import com.google.api.ads.dfa.axis.v1_20.UserRoleSearchCriteria;
import com.google.api.ads.dfa.axis.v1_20.UserSearchCriteria;
import com.google.api.ads.dfa.lib.client.DfaSession;
import com.google.api.client.auth.oauth2.Credential;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
	    // Generate a refreshable OAuth2 credential, which replaces legacy passwords
	    // and can be used in place of a service account.
	    Credential oAuth2Credential = new com.google.api.ads.common.lib.auth.OfflineCredentials.Builder()
	        .forApi(OfflineCredentials.Api.DFA)
	        .fromFile()
	        .build()
	        .generateCredential();

	    // Construct a DfaSession.
	    DfaSession session = new DfaSession.Builder()
	        .fromFile()
	        .withOAuth2Credential(oAuth2Credential)
	        .build();

	    // Construct a DfaServices.
	    DfaServices dfaServices = new DfaServices();
	    
	    // Request the user role service.
	    UserRoleRemote userRoleRemote = dfaServices.get(session, UserRoleRemote.class);
	    	    
	    // Define the old user role search criteria.
	    UserRoleSearchCriteria oldUserRoleSearchCriteria = new UserRoleSearchCriteria();
	    oldUserRoleSearchCriteria.setSearchString("Advertiser/Reporting Login");
	    // Set user role sort order.
	    SortOrder oldUserRoleSortOrder = new SortOrder();
	    oldUserRoleSortOrder.setFieldName("id");
	    oldUserRoleSortOrder.setDescending(false);
	    oldUserRoleSearchCriteria.setSortOrder(oldUserRoleSortOrder);
	    
	    // Get user role that matches the old user role search string.
	    long oldUserRoleId = 0;
	    UserRoleRecordSet oldUserRoleRecordSet = userRoleRemote.getUserRoles(oldUserRoleSearchCriteria);
	    UserRole[] oldUserRoles = oldUserRoleRecordSet.getUserRoles();
	    for (UserRole oldUserRole: oldUserRoles) {
	    	if (oldUserRole.getSubnetworkId() == 0) {
	    	  oldUserRoleId = oldUserRoles[0].getId();
	    	}
	    }
	    
	    // Define the new user role search criteria.
	    UserRoleSearchCriteria newUserRoleSearchCriteria = new UserRoleSearchCriteria();
	    newUserRoleSearchCriteria.setSearchString("Advertiser/Reporting Login");
	    // Set user role sort order.
	    SortOrder newUserRoleSortOrder = new SortOrder();
	    newUserRoleSortOrder.setFieldName("id");
	    newUserRoleSortOrder.setDescending(false);
	    newUserRoleSearchCriteria.setSortOrder(newUserRoleSortOrder);
	    
	    // Get user role that matches the new user role search string.
	    long newUserRoleId = 0;
	    UserRoleRecordSet newUserRoleRecordSet = userRoleRemote.getUserRoles(newUserRoleSearchCriteria);
	    UserRole[] newUserRoles = newUserRoleRecordSet.getUserRoles();
	    for (UserRole newUserRole: newUserRoles) {
	    	if (newUserRole.getSubnetworkId() == 0) {
	    	  newUserRoleId = newUserRoles[0].getId();
	    	}
	    }

    	// Request the user service.
	    UserRemote userRemote = dfaServices.get(session, UserRemote.class);
	    
	    // Define the user search criteria.
	    UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
	    // Set for active profiles only.
	    ActiveFilter activeFilter = new ActiveFilter(); 
	    activeFilter.isActiveOnly();
	    userSearchCriteria.setActiveFilter(activeFilter);
	    // Set for matching user role only.
	    userSearchCriteria.setUserRoleId(oldUserRoleId);
	    
	    // Get users that match the user search criteria.
	    UserRecordSet userRecordSet = userRemote.getUsersByCriteria(userSearchCriteria);
	    
	    // Change user roles.
	    /* KEEP WORKING ON THIS */
	}

}
