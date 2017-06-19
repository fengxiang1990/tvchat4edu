package com.bizcom.vo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.bizcom.util.DateUtil;
import com.bizcom.util.V2Log;

/**
 * Group information
 * 
 * @author 28851274
 * 
 */
public abstract class Group implements Comparable<Group> {
	protected long mGId;
	protected int mGroupType;
	protected String mName;

	protected long mOwner;
	protected User mOwnerUser;
	protected Date mCreateDate;
	protected Group mParent;
	protected List<Group> mChild;
	protected Set<User> users;
	protected int level;

	private Lock mLock = new ReentrantLock();

	/**
	 * @param gId
	 * @param groupType
	 * @param name
	 * @param owner
	 */
	protected Group(long gId, int groupType, String name, User owner) {
		this(gId, groupType, name, owner, new Date());

	}

	/**
	 * 
	 * @param gId
	 * @param groupType
	 * @param name
	 * @param owner
	 * @param createDate
	 */
	protected Group(long gId, int groupType, String name, User owner, Date createDate) {
		this.mGId = gId;
		this.mGroupType = groupType;
		this.mName = name;
		this.mOwnerUser = owner;
		this.mCreateDate = createDate;
		users = new CopyOnWriteArraySet<>();
		mChild = new CopyOnWriteArrayList<>();
		level = 1;
	}

	public long getGroupID() {
		return mGId;
	}

	public int getGroupType() {
		return mGroupType;
	}

	public String getName() {
		return mName;
	}

	public Date getCreateDate() {
		return mCreateDate;
	}

	public String getStrCreateDate() {
		if (mCreateDate != null) {
			return DateUtil.getStringDate(mCreateDate.getTime());
		} else {
			return null;
		}
	}
	
	public String[] getStrCovFormatDate(){
		if (mCreateDate != null) {
			return DateUtil.getDateForTabFragmentConversation(mCreateDate.getTime());
		} else {
			return null;
		}
	}

	public User getOwnerUser() {
		return mOwnerUser;
	}

	public User getUser(long userID) {
		Iterator<User> iterator = users.iterator();
		while(iterator.hasNext()){
			User temp = iterator.next();
			if (temp.getmUserId() == userID) {
				return temp;
			}
		}
		return null;
	}

	public Group getParent() {
		return mParent;
	}

	/**
	 * return copy collection
	 * 
	 * @return
	 */
	public List<User> getUsers() {
		return new ArrayList<>(this.users);
	}

	public List<Group> getChildGroup() {
		return this.mChild;
	}

	/**
	 * Get sub group and user sum
	 * 
	 * @return
	 */
	public int getSubSize() {
		return this.mChild.size() + this.users.size();
	}

	public int getOnlineUserCount() {
		// FIXME should optimze data structure
		Set<User> counter = new HashSet<>();
		this.populateUser(this, counter);

		int c = 0;
		Iterator<User> iterator = counter.iterator();
		while(iterator.hasNext()){
			User u = iterator.next();
			if (u.getmStatus() == User.Status.ONLINE || u.getmStatus() == User.Status.BUSY
					|| u.getmStatus() == User.Status.DO_NOT_DISTURB || u.getmStatus() == User.Status.LEAVE) {
				c++;
			}
		}
		return c;
	}

	public Set<User> getOnlineUserSet() {
		Set<User> counter = new HashSet<>();
		this.populateUser(this, counter);
		return counter;
	}

	public int getUserCount() {
		Set<User> counter = new HashSet<>();
		populateUser(this, counter);
		int count = counter.size();
		counter.clear();
		return count;
	}

	public int getLevel() {
		return level;
	}

	public void setGId(long mGId) {
		this.mGId = mGId;
	}

	public void setGroupType(int mGroupType) {
		this.mGroupType = mGroupType;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public void setCreateDate(Date createDate) {
		this.mCreateDate = createDate;
	}

	public void setOwnerUser(User mOwnerUser) {
		this.mOwnerUser = mOwnerUser;
	}

	public void setOwner(long mOwner) {
		this.mOwner = mOwner;
	}

	public void setParent(Group parent) {
		if (parent == null) {
			mParent = null;
			level = 1;
		} else {
			this.mParent = parent;
			level = this.getParent().getLevel() + 1;
		}
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void addGroupToGroup(Group g) {
		mLock.lock();
		try {
			if (g == null) {
				V2Log("addGroupToGroup: Invalid group data");
				return;
			}

			mChild.add(g);
			g.setParent(this);
		} finally {
			mLock.unlock();
		}
	}

	public void addUserToGroup(Collection<User> users) {
		mLock.lock();
		try {
			if (users == null) {
				V2Log("addUserToGroup: Invalid user data");
				return;
			}

			Iterator<User> iterator = users.iterator();
			while(iterator.hasNext()){
				User temp = iterator.next();
				this.users.add(temp);
				temp.addUserToGroup(this);
			}
		} finally {
			mLock.unlock();
		}
	}

	public void addUserToGroup(User u) {
		mLock.lock();
		try {
			if (u == null) {
				V2Log("addUserToGroup: Invalid user data");
				return;
			}

			users.add(u);
			u.addUserToGroup(this);
		} finally {
			mLock.unlock();
		}
	}

	public void removeGroupFromGroup(Group g) {
		mLock.lock();
		try {
			if (g == null) {
				V2Log("removeGroupFromGroup: Invalid group data");
				return;
			}

			mChild.remove(g);
			g.setParent(null);
		} finally {
			mLock.unlock();
		}
	}

	public void removeUserFromGroup(User u) {
		removeUserFromGroup(u.getmUserId());
	}

	public void removeUserFromGroup(long uid) {
		mLock.lock();
		try {
			// User object use id as identification

			Iterator<User> iterator = users.iterator();
			while(iterator.hasNext()){
				User user = iterator.next();
				if (user.getmUserId() == uid) {
					users.remove(user);
					user.removeUserFromGroup(this);
					break;
				}
			}
		} finally {
			mLock.unlock();
		}
	}
	
	public void clearUsers() {
		mLock.lock();
		try {
			// User object use id as identification
			Iterator<User> iterator = users.iterator();
			while(iterator.hasNext()) {
				User user = iterator.next();
				users.remove(user);
				user.removeUserFromGroup(this);
			}
		} finally {
			mLock.unlock();
		}
	}

	/**
	 * Find use in current group and childs group.<br>
	 * If find and return first belongs group.
	 * 
	 * @param u
	 * @return
	 */
	public Group findUser(User u) {
		if (u == null) {
			return null;
		}
		return internalSearchUser(null, u.getmUserId());
	}

	/**
	 * Find use in current group and childs group.<br>
	 * If find and return first belongs group.
	 * 
	 * @param userID
	 * @return
	 */
	public Group findUser(long userID) {
		return internalSearchUser(null, userID);
	}

	public Group internalSearchUser(Group g, long userID) {
		if (g == null) {
			g = this;
		}
		List<User> list = this.getUsers();
		for (User tu : list) {
			if (tu.getmUserId() == userID) {
				return g;
			}
		}
		List<Group> subGroups = g.getChildGroup();
		for (int i = 0; i < subGroups.size(); i++) {
			Group subG = subGroups.get(i);
			Group gg = internalSearchUser(subG, userID);
			if (gg != null) {
				return gg;
			}
		}
		return null;

	}

	public List<User> searchUser(String text) {
		List<User> l = new ArrayList<>();
		Group.searchUser(text, l, this);
		return l;
	}

	public static void searchUser(String text, List<User> l, Group g) {
		if (l == null || g == null) {
			return;
		}
		List<User> list = g.getUsers();
		for(int i = 0 ; i < list.size() ; i++){
			User u = list.get(i);
			if ((u.getDisplayName() != null && u.getDisplayName().contains(text))
					|| u.getArra().equals(text)) {
				l.add(u);
			}
		}

		List<Group> childs = g.getChildGroup();
		for(int i = 0 ; i < childs.size() ; i++){
			Group child = childs.get(i);
			searchUser(text, l, child);
		}
	}

	private void populateUser(Group g, Set<User> counter) {
		List<User> lu = g.getUsers();
		for (int i = 0; i < lu.size(); i++) {
			counter.add(lu.get(i));
		}
		List<Group> sGs = g.getChildGroup();
		for (int i = 0; i < sGs.size(); i++) {
			Group subG = sGs.get(i);
			populateUser(subG, counter);
		}
	}

	public void V2Log(String log) {
		V2Log.e("Group --> " + log);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (mGId ^ (mGId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (mGId != other.mGId)
			return false;
		return true;
	}

	@Override
	public int compareTo(Group arg0) {
		return 0;
	}

	public abstract String toXml();
}
