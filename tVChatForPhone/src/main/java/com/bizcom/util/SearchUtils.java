package com.bizcom.util;

import android.text.Editable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;

import com.bizcom.vc.activity.conference.LeftAttendeeListLayout.Wrapper;
import com.bizcom.vc.adapter.SimpleBaseAdapter;
import com.bizcom.vo.Attendee;
import com.bizcom.vo.Conversation;
import com.bizcom.vo.User;
import com.config.GlobalConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchUtils {

    private static final String TAG = "SearchUtils";
    private List<Object> searchList = new ArrayList<>();
    private List<Object> searchCacheList = new ArrayList<>();
    private List<Object> surplusList = new ArrayList<>();
    private List<Object> singleCacheList = new ArrayList<>();
    private SparseArray<List<Object>> contentCacheList = new SparseArray<>();
    private SparseArray<String> contentLengthCacheList = new SparseArray<>();
    public List<Object> receiveList = new ArrayList<>();

    private boolean isShouldAdd;
    private boolean isShouldQP; // 是否需要启动全拼
    private int startIndex = 0;
    private boolean isBreak; // 用于跳出getSearchList函数中的二级循环
    private boolean mIsStop; // 用于终止搜索
    public boolean mIsStartedSearch = false;

    private final int TYPE_CONVERSATION = 10;
    private final int TYPE_ITEM_DATA = 11;
    private final int TYPE_WRAPPER = 12;
    private final int TYPE_GLOBAL_LIST_ITEM = 13;
    private int type = TYPE_CONVERSATION;

    public static class ScrollItem implements Comparable<ScrollItem> {
        public Conversation cov;
        public View gp;
        private String name;
        private boolean isInNameSort;

        public ScrollItem(Conversation g, View gp, boolean isInNameSort) {
            super();
            this.cov = g;
            this.gp = gp;
            this.gp.setTag(cov);
            this.isInNameSort = isInNameSort;
            name = cov.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ScrollItem)) {
                return false;
            }

            final ScrollItem other = (ScrollItem) o;
            if (other.cov == null) {
                return false;
            }

            if (this.name == null) {
                return false;
            }

            if (this.name.equals(other.cov.getName())) {
                return cov.getExtId() == other.cov.getExtId();
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int code = name == null ? 0 : name.hashCode();
            return (int) (37 * code * cov.getExtId());
        }

        @Override
        public int compareTo(ScrollItem item) {
            if (item == null)
                return 1;

            if (cov.getExtId() == item.cov.getExtId())
                return 0;

            if (isInNameSort) {
                if (cov.getName() == null) {
                    return -1;
                } else {
                    if (cov.getName().equals(item.cov.getName())) {
                        if (cov.getExtId() == item.cov.getExtId()) {
                            return 0;
                        } else {
                            return 1;
                        }
                    }
                    return cov.getName().compareTo(item.cov.getName());
                }
            } else {
                long localTime = cov.getDateLong();
                long remoteTime = item.cov.getDateLong();
                if (localTime < remoteTime)
                    return 1;
                else if (localTime > remoteTime) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    /**
     * For ConversationTabFragment search
     *
     * @param content
     * @return
     */
    public List<ScrollItem> startScrollItemSearch(Editable content) {
        type = TYPE_CONVERSATION;

        List<Object> cache = contentCacheList.get(content.length());
        if (cache != null && contentLengthCacheList.get(content.length()) != null
                && contentLengthCacheList.get(content.length()).equals(content.toString())) {
            V2Log.d(TAG, "find cache list : key --> " + content.length() + " and value --> " + cache.size());
            List<ScrollItem> cacheItems = new ArrayList<>();
            for (Object object : cache) {
                cacheItems.add((ScrollItem) object);
            }
            contentCacheList.delete(content.length() + 1);
            contentLengthCacheList.delete(content.length() + 1);
            return cacheItems;
        } else {
            searchList.clear();
            if (content.length() - 1 != 0) {
                startIndex = content.length() - 1;
                List<Object> lastCache = contentCacheList.get(content.length() - 1);
                if (lastCache != null)
                    searchList.addAll(lastCache);
            } else {
                startIndex = 0;
                mIsStartedSearch = false;
            }

            List<ScrollItem> searchItems = new ArrayList<>();
            List<Object> search = search(content.toString());
            if (search != null && search.size() > 0) {
                for (Object object : search) {
                    searchItems.add((ScrollItem) object);
                }

                List<Object> temp = new ArrayList<>();
                temp.addAll(search);
                contentCacheList.put(content.length(), temp);
                contentLengthCacheList.put(content.length(), content.toString());
                V2Log.d(TAG, "put cache list : key --> " + content.length() + " and value --> " + search.size());
            }
            return searchItems;
        }
    }

    /**
     * For ConversationTabFragment search
     *
     * @param content
     * @return
     */
    public List<Conversation> startConversationSearch(Editable content) {
        type = TYPE_CONVERSATION;

        List<Object> cache = contentCacheList.get(content.length());
        if (cache != null && contentLengthCacheList.get(content.length()) != null
                && contentLengthCacheList.get(content.length()).equals(content.toString())) {
            V2Log.d(TAG, "find cache list : key --> " + content.length() + " and value --> " + cache.size());
            List<Conversation> cacheItems = new ArrayList<>();
            for (Object object : cache) {
                cacheItems.add((Conversation) object);
            }
            contentCacheList.delete(content.length() + 1);
            contentLengthCacheList.delete(content.length() + 1);
            return cacheItems;
        } else {
            searchList.clear();
            if (content.length() - 1 != 0) {
                startIndex = content.length() - 1;
                List<Object> lastCache = contentCacheList.get(content.length() - 1);
                if (lastCache != null)
                    searchList.addAll(lastCache);
            } else {
                startIndex = 0;
                mIsStartedSearch = false;
            }

            List<Conversation> searchItems = new ArrayList<>();
            List<Object> search = search(content.toString());
            if (search != null && search.size() > 0) {
                for (Object object : search) {
                    searchItems.add((Conversation) object);
                }

                List<Object> temp = new ArrayList<>();
                temp.addAll(search);
                contentCacheList.put(content.length(), temp);
                contentLengthCacheList.put(content.length(), content.toString());
                V2Log.d(TAG, "put cache list : key --> " + content.length() + " and value --> " + search.size());
            }
            return searchItems;
        }
    }

    /**
     * 会议中参会人搜索
     *
     * @param content
     * @return
     */
    public List<Wrapper> startVideoAttendeeSearch(String content) {
        type = TYPE_WRAPPER;

        List<Object> cache = contentCacheList.get(content.length());
        if (cache != null && contentLengthCacheList.get(content.length()) != null
                && contentLengthCacheList.get(content.length()).equals(content.toString())) {
            V2Log.d(TAG, "find cache list : key --> " + content.length() + " and value --> " + cache.size());
            List<Wrapper> cacheItems = new ArrayList<>();
            for (Object object : cache) {
                cacheItems.add((Wrapper) object);
            }
            contentCacheList.delete(content.length() + 1);
            return cacheItems;
        } else {
            searchList.clear();
            if (content.length() - 1 != 0) {
                startIndex = content.length() - 1;
                List<Object> lastCache = contentCacheList.get(content.length() - 1);
                if (lastCache != null)
                    searchList.addAll(lastCache);
            } else {
                startIndex = 0;
                mIsStartedSearch = false;
            }

            List<Wrapper> searchItems = new ArrayList<>();
            List<Object> search = search(content);
            if (search != null && search.size() > 0) {
                for (Object object : search) {
                    searchItems.add((Wrapper) object);
                }

                List<Object> temp = new ArrayList<>();
                temp.addAll(search);
                contentCacheList.put(content.length(), temp);
                contentLengthCacheList.put(content.length(), content);
                V2Log.d(TAG, "put cache list : key --> " + content.length() + " and value --> " + search.size());
            }
            return searchItems;
        }
    }

    /**
     * 本地全局搜索
     *
     * @param content
     * @return
     */
    public List<SimpleBaseAdapter.ListItem> startGlobalLocalSearch(String content) {
        type = TYPE_GLOBAL_LIST_ITEM;

        List<Object> cache = contentCacheList.get(content.length());
        if (cache != null && contentLengthCacheList.get(content.length()) != null
                && contentLengthCacheList.get(content.length()).equals(content)) {
            List<SimpleBaseAdapter.ListItem> cacheItems = new ArrayList<>();
            for (Object object : cache) {
                cacheItems.add((SimpleBaseAdapter.ListItem) object);
            }
            contentCacheList.delete(content.length() + 1);
            return cacheItems;
        } else {
            searchList.clear();
            if (content.length() - 1 != 0) {
                startIndex = content.length() - 1;
                List<Object> lastCache = contentCacheList.get(content.length() - 1);
                if (lastCache != null)
                    searchList.addAll(lastCache);
            } else {
                startIndex = 0;
                mIsStartedSearch = false;
            }

            List<SimpleBaseAdapter.ListItem> searchItems = new ArrayList<>();
            List<Object> search = search(content);
            if (search != null && search.size() > 0) {
                for (Object object : search) {
                    searchItems.add((SimpleBaseAdapter.ListItem) object);
                }

                List<Object> temp = new ArrayList<>();
                temp.addAll(search);
                contentCacheList.put(content.length(), temp);
                contentLengthCacheList.put(content.length(), content);
                V2Log.d(TAG, "put cache list : key --> " + content.length() + " and value --> " + search.size());
            }
            return searchItems;
        }
    }

    /**
     * For GroupListView Filter
     *
     * @param content
     * @return
     */
    public List<User> receiveGroupUserFilterSearch(String content) {
        List<User> result = new ArrayList<>();
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int count = i;
            String target = String.valueOf(chars, 0, ++count);
            result = startGroupUserFilterSearch(target);
        }
        return result;
    }

    /**
     * For GroupListView Filter
     *
     * @param content
     * @return
     */
    public List<User> startGroupUserFilterSearch(String content) {
        type = TYPE_ITEM_DATA;

        List<Object> cache = contentCacheList.get(content.length());
        if (cache != null && contentLengthCacheList.get(content.length()) != null
                && contentLengthCacheList.get(content.length()).equals(content)) {
            V2Log.d(TAG, "find cache list : key --> " + content.length() + " and value --> " + cache.size());
            List<User> cacheUsers = new ArrayList<>();
            for (Object object : cache) {
                cacheUsers.add((User) object);
            }
            contentCacheList.delete(content.length() + 1);
            contentLengthCacheList.delete(content.length() + 1);
            return cacheUsers;
        } else {
            searchList.clear();
            if (content.length() - 1 != 0) {
                startIndex = content.length() - 1;
                List<Object> lastCache = contentCacheList.get(content.length() - 1);
                if (lastCache != null)
                    searchList.addAll(lastCache);
            } else {
                startIndex = 0;
                mIsStartedSearch = false;
            }

            List<User> searchUsers = new ArrayList<>();
            List<Object> search = search(content);
            if (search != null && search.size() > 0) {
                for (Object object : search) {
                    searchUsers.add((User) object);
                }

                List<Object> temp = new ArrayList<>();
                temp.addAll(search);
                contentCacheList.put(content.length(), temp);
                contentLengthCacheList.put(content.length(), content);
                V2Log.d(TAG, "put cache list : key --> " + content.length() + " and value --> " + search.size());
            }
            return searchUsers;
        }
    }

    private List<Object> search(String content) {
        if (content != null && content.length() > 0) {
            if (!mIsStartedSearch) {
                mIsStartedSearch = true;
                searchList.addAll(receiveList);
            }

            V2Log.d(TAG, "Editable :" + content);
            char[] charSimpleArray = content.toLowerCase(Locale.getDefault()).toCharArray();
            // 搜字母查询
            for (int i = startIndex; i < charSimpleArray.length; i++) {
                if(mIsStop){
                    return searchList;
                }

                if (isChineseWord(charSimpleArray[i])) {
                    V2Log.d(TAG, "--- " + charSimpleArray[i] + " ---is Chinese");
                    searchCacheList = getSearchList(searchList, String.valueOf(charSimpleArray[i]), content,
                            i, true, true);
                } else {
                    V2Log.d(TAG, "--- " + charSimpleArray[i] + " ---not Chinese");
                    searchCacheList = getSearchList(searchList, String.valueOf(charSimpleArray[i]), content,
                            i, true, false);
                }

                searchList.clear();
                if (searchCacheList.size() > 0) {
                    isShouldQP = false;
                    singleCacheList.addAll(searchCacheList);
                    searchCacheList.clear();

                    V2Log.d(TAG, "简拼找到结果 , 全拼再搜索一遍");
                    if (surplusList.size() > 0) {
                        searchCacheList.addAll(surplusList);
                        surplusList.clear();
                    }
                    startQPSearch(content);
                    for (int j = 0 ; j < singleCacheList.size() ; j++){
                        Object temp = singleCacheList.get(j);
                        if(!searchList.contains(temp)){
                            searchList.add(temp);
                        }
                    }
                    searchCacheList.clear();
                    singleCacheList.clear();
                } else {
                    isShouldQP = true;
                    List<Object> cache = contentCacheList.get(content.length() - 1);
                    if (cache != null)
                        searchCacheList.addAll(contentCacheList.get(content.length() - 1));
                    V2Log.d(TAG, "简拼没有结果 开启全拼搜索");
                }
            }

            if (isShouldQP) { // 如果长度大于5则不按首字母查询
                searchList.clear();
                startQPSearch(content);
            }
            startIndex++;
            return searchList;
        } else {
            clearAll();
        }
        return searchList;
    }

    public void clearAll() {
        if (mIsStartedSearch) {
            receiveList.clear();
            searchCacheList.clear();
            searchList.clear();
            contentCacheList.clear();
            surplusList.clear();
            singleCacheList.clear();
            mIsStartedSearch = false;
            startIndex = 0;
        }
    }

    public void stopSearching(boolean mIsStop){
        this.mIsStop = mIsStop;
    }

    public void startQPSearch(String content) {
        content = content.toLowerCase();
        V2Log.d(TAG, "全拼搜索的集合大小：" + searchCacheList.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < searchCacheList.size(); i++) {
            if(mIsStop){
                return ;
            }

            Object obj = searchCacheList.get(i);
            V2Log.d(TAG, "current search word : " + getObjectValue(obj));
            if (TextUtils.isEmpty(getObjectValue(obj))) {
                return;
            }

            // 获取名字，将名字变成拼音串起来
            // FIXME 包含有多音字，需要重新组合
            char[] charArray = getObjectValue(obj).toCharArray();
            for (char c : charArray) {
                String charStr;
                if (isChineseWord(c)) {
                    charStr = GlobalConfig.CHINESE_CHAR_ARRAY.get(String.valueOf(c));
                    if(charStr != null){
                        String[] split = charStr.split(";");
                        charStr = split[0];
                    }
                } else {
                    charStr = String.valueOf(c);
                }
                sb.append(charStr);
            }
            V2Log.d(TAG, "current searh material : " + sb.toString());
            String material = sb.toString().toLowerCase();
            // 判断该昵称第一个字母，与输入的第一字母是否匹配
            Character first = material.toCharArray()[0];
            char[] targetChars = content.toCharArray();
            if (!first.equals(targetChars[0])) {
                isShouldAdd = true;
            } else {
                for (int j = 0; j < targetChars.length; j++) {
                    if (j >= material.length() || targetChars[j] != material.charAt(j)) {
                        isShouldAdd = true;
                        V2Log.d(TAG, "material not contains " + targetChars[j]);
                        break;
                    }
                    isShouldAdd = false;
                }
            }

            if (!isShouldAdd) {
                String shouldAdd = getObjectValue(obj);
                if (!searchList.contains(obj)){
                    V2Log.d(TAG, "added ---------" + shouldAdd);
                    searchList.add(obj);
                }
            } else {
                if (searchList.contains(obj))
                    searchList.remove(obj);
            }
            sb.delete(0, sb.length());
        }
    }

    /**
     * 根据 searchKey 获得搜索后的集合
     *
     * @param list
     * @param searchKey
     * @param content
     * @param index
     * @param isFirstSearch 判断是否是首字母搜索
     * @param isChinese     判断searchKey是否为中文
     * @return
     */
    public List<Object> getSearchList(List<Object> list, String searchKey, String content, int index,
                                      boolean isFirstSearch, boolean isChinese) {
        V2Log.d(TAG, "getSearchList--> searchList : " + list.size() + " | searchKey : " + searchKey + " | startIndex : "
                + index + " | content : " + content);
        List<Object> tempList = new ArrayList<>();
        if (searchKey == null || searchKey.length() < 0) {
            return tempList;
        }

        String searchTarget;
        for (int i = 0; i < list.size(); i++) { // 一级循环，循环所有消息
            if(mIsStop){
                return tempList;
            }

            boolean isAdd = false;
            Object obj = list.get(i);
            // 判断是否能获取到消息item的名字
            if (getObjectValue(obj) != null) {
                // 将名字分割为字符数组遍历
                char[] charArray = getObjectValue(obj).toCharArray();
                for (int j = index; j < charArray.length; j++) { // 二级循环，循环消息名称
                    searchTarget = String.valueOf(charArray[j]);
                    if (isFirstSearch && isChinese) {
                        if (searchKey.contains(searchTarget)) {
                            isAdd = true;
                            break;
                        }
                    } else if (isFirstSearch) {
                        // if (isChineseWord(cov.getName().charAt(index))) {
                        if (isChineseWord(charArray[j])) {
                            String englishChar = GlobalConfig.CHINESE_CHAR_ARRAY.get(searchTarget);
                            V2Log.d(TAG, "englishChar :" + englishChar);
                            if (englishChar == null)
                                continue;
                            String[] split = englishChar.split(";");
                            for (String string : split) { // 三级循环，循环多音字
                                int indexOf = string.indexOf(searchKey);
                                if (indexOf == 0) { // &&
                                    // content.indexOf(searchKey)
                                    // == index
                                    isAdd = true;
                                    isBreak = true;
                                    break;
                                }
                            }
                            // tempList添加元素后就直接跳出二级循环。
                            if (isBreak) {
                                isBreak = false;
                                break;
                            }
                            // if(searchTarget.contains(searchKey)){
                        } else {
                            searchTarget = searchTarget.toLowerCase(Locale.getDefault());
                            V2Log.d(TAG, "searchTarget :" + searchTarget);
                            int indexOf = searchTarget.indexOf(searchKey);
                            // if(searchTarget.contains(searchKey)){
                            if (indexOf != -1) {
                                isAdd = true;
                                break;
                            }
                        }
                    }
                }

                if (isAdd) {
                    tempList.add(obj);
                } else {
                    surplusList.add(obj);
                }
            }
        }
        return tempList;
    }

    public String getObjectValue(Object obj) {

        switch (type) {
            case TYPE_CONVERSATION:
                return ((ScrollItem) obj).cov.getName();
            case TYPE_ITEM_DATA:
                return ((User) obj).getDisplayName();
            case TYPE_WRAPPER:
                Attendee att = ((Wrapper) obj).a;
                if (att.getType() == Attendee.TYPE_MIXED_VIDEO)
                    return GlobalConfig.Resource.MIX_VIDEO_DEFAULT_NAME;
                else
                    return att.getAttName();
            case TYPE_GLOBAL_LIST_ITEM:
                SimpleBaseAdapter.ListItem item = (SimpleBaseAdapter.ListItem) obj;
                Conversation cov = (Conversation) item.mEntity;
                return cov.getName();
            default:
                break;
        }
        return null;
    }

    /**
     * 判断给定的字符是否为汉字
     *
     * @param mChar
     * @return
     */
    public boolean isChineseWord(char mChar) {
        Pattern pattern = Pattern.compile("[\\u4E00-\\u9FA5]"); // 判断是否为汉字
        Matcher matcher = pattern.matcher(String.valueOf(mChar));
        return matcher.find();
    }
}
