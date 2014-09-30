package main.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.dfa.axis.factory.DfaServices;
import com.google.api.ads.dfa.axis.v1_20.ActiveFilter;
import com.google.api.ads.dfa.axis.v1_20.SortOrder;
import com.google.api.ads.dfa.axis.v1_20.User;
import com.google.api.ads.dfa.axis.v1_20.UserRecordSet;
import com.google.api.ads.dfa.axis.v1_20.UserRemote;
import com.google.api.ads.dfa.axis.v1_20.UserRole;
import com.google.api.ads.dfa.axis.v1_20.UserRoleRecordSet;
import com.google.api.ads.dfa.axis.v1_20.UserRoleRemote;
import com.google.api.ads.dfa.axis.v1_20.UserRoleSearchCriteria;
import com.google.api.ads.dfa.axis.v1_20.UserSaveResult;
import com.google.api.ads.dfa.axis.v1_20.UserSearchCriteria;
import com.google.api.ads.dfa.lib.client.DfaSession;
import com.google.api.client.auth.oauth2.Credential;

public class Main {
	
	public static String oldUserRoleName = "Advertiser/Reporting Login";
	public static String newUserRoleName = "Vendor Reporting";
	
	private static File file;
	private static FileWriter fw;
	private static BufferedWriter bw;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		createFile("UserRoleChange.log");
		
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
	    oldUserRoleSearchCriteria.setSearchString(oldUserRoleName);
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
	    	if (oldUserRole.getSubnetworkId() == 0 && oldUserRole.getName().equals(oldUserRoleName)) { // if no subnetwork and old user role name matches exactly
	    	    oldUserRoleId = oldUserRole.getId();
	    	    out("Old User Role Id is " + oldUserRoleId + ".\n", bw);
	    	}
	    }

	    // Define the new user role search criteria.
	    UserRoleSearchCriteria newUserRoleSearchCriteria = new UserRoleSearchCriteria();
	    newUserRoleSearchCriteria.setSearchString(newUserRoleName);
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
	    	if (newUserRole.getSubnetworkId() == 0 && newUserRole.getName().equals(newUserRoleName)) { // if no subnetwork and new user role name matches exactly
	    	    newUserRoleId = newUserRole.getId();
	    	    out("New User Role Id is " + newUserRoleId + ".\n", bw);
	    	}
	    }

	    // Proceed only if valid user role ids have been found
	    if (oldUserRoleId != 0 && newUserRoleId != 0) {
	    	out("Valid Ids for old user role and new user role. Making role change for users.\n", bw);
	    	
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
		    User[] users = userRecordSet.getRecords();
		    if (users.length > 0) {
		    	out("Users with role change:\n", bw);
		    }
		    UserSaveResult[] userSaveResults = new UserSaveResult[users.length];
		    for (int u=0;u<users.length;u++) {
		    	users[u].setUserGroupId(newUserRoleId);
		    	userSaveResults[u] = userRemote.saveUser(users[u]);
		    }
		    
		    // Print results.
		    userSearchCriteria.setUserRoleId(newUserRoleId);
		    UserRecordSet resultsUserRecordSet = userRemote.getUsersByCriteria(userSearchCriteria);
		    for (User user:resultsUserRecordSet.getRecords()) {
		    	out(user.getId() + "\t" + user.getName() + "\t" + user.getUserGroupId() + "\n", bw);		    	
		    }
	    } else {
	    	out("Invalid Id for old user role and/or new user role. Skipping role change for users.\n", bw);
	    }
	    out("Complete.\n", bw);
		bw.close();
	    fw.close();
	}
	
	// helper function to create output files
	private static void createFile(String fileName) throws IOException {
		
		file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		fw = new FileWriter(file.getAbsoluteFile());
		bw = new BufferedWriter(fw);
	}
	
	// helper function to dual-stream text to CLI and text file
	private static void out(String text, BufferedWriter bw) throws IOException {
		// write to file
		bw.write(text);
		// print to CLI
		System.out.print(text);
	}

}
