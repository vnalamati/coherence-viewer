package com.zh.coherence.viewer.tools.statistic.report;

import com.zh.coherence.viewer.jmx.JMXManager;
import com.zh.coherence.viewer.tools.statistic.report.cache.CacheInfo;
import com.zh.coherence.viewer.tools.statistic.report.cache.CacheNodeInfo;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.*;

public class CacheReport implements Named{

    private Map<String, CacheInfo> data = new HashMap<String, CacheInfo>();

    private long totalUnits = 0;

    public void updateData() {
        long time = System.currentTimeMillis();
        try{
            data.clear();
            totalUnits = 0;

            MBeanServerConnection server = JMXManager.getInstance().getServer();
            Set<ObjectName> cacheNamesSet = server.queryNames(new ObjectName("Coherence:type=Cache,*"), null);
            for (Object aCacheNamesSet : cacheNamesSet) {
                ObjectName cacheNameObjName = (ObjectName) aCacheNamesSet;
                String name = cacheNameObjName.getKeyProperty("name");
                if(!data.containsKey(name)){
                    data.put(name, new CacheInfo(name));
                }
                CacheNodeInfo cacheNodeInfo = new CacheNodeInfo();
                cacheNodeInfo.setName(cacheNameObjName.getKeyProperty("nodeId"));
                AttributeList attrs = server.getAttributes(
                        cacheNameObjName,
                        new String[]{"Size", "TotalPuts", "TotalGets", "CacheHits", "AverageGetMillis", "Units"});
                List<Attribute> attributes = attrs.asList();
                Integer size = (Integer) attributes.get(0).getValue();
                cacheNodeInfo.setSize(size);
                totalUnits += size;
                Long totalPuts = (Long) attributes.get(1).getValue();
                cacheNodeInfo.setTotalPuts(totalPuts);
                Long totalGets = (Long) attributes.get(2).getValue();
                cacheNodeInfo.setTotalGets(totalGets);
                Long cacheHits = (Long) attributes.get(3).getValue();
                cacheNodeInfo.setCacheHits(cacheHits);
                Double averageGetMillis = (Double) attributes.get(4).getValue();
                cacheNodeInfo.setAverageGetMillis(averageGetMillis);
                cacheNodeInfo.setUnits(Long.valueOf(attributes.get(5).getValue().toString()));

                data.get(name).addCacheNodeInfo(cacheNodeInfo);
            }
            System.err.println("cache report time : " + (System.currentTimeMillis() - time));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public List<CacheInfo> getData() {
        return new ArrayList<CacheInfo>(data.values());
    }

    public void setData(Map<String, CacheInfo> data) {
        this.data = data;
    }

    public long getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(long totalUnits) {
        this.totalUnits = totalUnits;
    }

    @Override
    public String getName() {
        return "Cache report";
    }
}
