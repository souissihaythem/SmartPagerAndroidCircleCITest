package net.smartpager.android;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 604524834819015651L;

	public List<Integer> mMarkAsRead;
	public List<Integer> mMarkAsAccepted;
	public List<Integer> mMarkAsRejected;
	public List<Integer> mMarkAsReplied;

	public User() {
		mMarkAsRead = new ArrayList<Integer>();
	}
	
	// ================================================================================
	// Serializable
	// ================================================================================

	/**
	 * Save user state on device
	 * 
	 * @param context
	 *            current context
	 * @param User
	 *            for save
	 * @return
	 */
	public static boolean Save(Context context, User user) {
		boolean result = true;
		try {
			FileOutputStream fileOut = context.openFileOutput("user.ser", Context.MODE_PRIVATE);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(user);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
		return result;
	}

	/**
	 * Load user state from device
	 * 
	 * @param context
	 *            current context
	 * @return User with saved information
	 */
	public static User Open(Context context) {
		User user = null;
		try {
			FileInputStream fileIn = context.openFileInput("user.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			user = (User) in.readObject();
			in.close();
			fileIn.close();

		} catch (IOException i) {

			//i.printStackTrace();
			return new User();
		} catch (ClassNotFoundException c) {

			c.printStackTrace();
			return null;
		}
		return user;
	}

}
