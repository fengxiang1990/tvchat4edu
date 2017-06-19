package com.bizcom.vc.hg.view;

import java.util.Comparator;

import com.bizcom.vc.hg.beans.PhoneFriendItem;

/**
 * @author xiaanming
 */
public class PinyinComparator implements Comparator<PhoneFriendItem> {

    public int compare(PhoneFriendItem o1, PhoneFriendItem o2) {
//        if (o1.isHasRegsited() == o2.isHasRegsited()) {
//            return 0;
//        } else {
//            return o2.isHasRegsited() ? 1 : -1;
//        }

        boolean b1 = o1.isHasRegsited();
        boolean b2 = o2.isHasRegsited();

        if (b1 != b2) {
            if (b1 == true) {
                return -1;
            }
            if (b1 == false) {
                return 1;
            }
        } else if (o1.getSortLetters().equals("@")
                || o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")
                || o2.getSortLetters().equals("@")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }

        return 0;
    }

}
