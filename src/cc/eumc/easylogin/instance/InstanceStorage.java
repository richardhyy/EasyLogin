package cc.eumc.easylogin.instance;

import cc.eumc.easylogin.instance.instanceinfo.InstanceEntry;

import java.util.HashMap;
import java.util.Map;

public class InstanceStorage {
    public InstanceEntry activeInstance;
    public Map<InstanceEntry, String> instances = new HashMap<>();
}
