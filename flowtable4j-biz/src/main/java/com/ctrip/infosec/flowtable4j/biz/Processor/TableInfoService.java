package com.ctrip.infosec.flowtable4j.biz.processor;

import com.ctrip.infosec.flowtable4j.model.persist.ColumnInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangsx on 2015/6/16.
 */
@Component
public class TableInfoService {

    private Map<String, List<ColumnInfo>> tableInfos;

    public  void  updateTableInfos(Map<String,List<ColumnInfo>> src){
        this.tableInfos = src;
    }

    public List<ColumnInfo> getTableInfo(String tableName) {
        if(tableInfos!=null && tableInfos.size()>0) {
            return tableInfos.get(tableName);
        }
        return null;
    }
}
