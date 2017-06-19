package com.bizcom.request.jni;

import com.V2.jni.ind.V2Group;

/**
 * Used to wrap response data from JNI when receive call from JNI
 *
 * @author 28851274
 */
public class CreateGroupResponse extends JNIResponse {


    private V2Group mGroup;
    long nGroupId;

    /**
     * This class is wrapper that wrap response of create crowd
     *
     * @param nGroupId returned crowd id
     * @param result   {@link Result}
     */
    public CreateGroupResponse(long nGroupId,
                               Result result) {
        this(nGroupId, null, result);
    }

    public CreateGroupResponse(long nGroupId, V2Group mGroup, Result res) {
        super(res);
        this.nGroupId = nGroupId;
        this.mGroup = mGroup;
    }

    public long getGroupId() {
        return nGroupId;
    }

    public V2Group getGroup() {
        return mGroup;
    }

    public void setGroup(V2Group mGroup) {
        this.mGroup = mGroup;
    }
}
