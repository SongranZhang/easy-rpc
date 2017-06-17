package com.linkedkeeper.easyrpc.config.api;


public class ProviderConfig extends AbstractInterfaceConfig {

    protected transient Object ref;

    /**
     * Gets ref.
     *
     * @return the ref
     */
    public Object getRef() {
        return ref;
    }

    /**
     * Sets ref.
     *
     * @param ref the ref
     */
    public void setRef(Object ref) {
        this.ref = ref;
    }

}
