package com.ctrip.infosec.flowtable4j.flowlist;

import com.ctrip.infosec.flowtable4j.model.FlowFact;

import java.util.List;
import java.util.Map;

/**
 * Created by thyang on 2015/3/24 0024.
 */
public class FieldMatchRuleTerm extends FlowRuleTerm {

    public FieldMatchRuleTerm(String fieldName, String operator, String matchField) {
        super(fieldName, operator, matchField);
    }

    @Override
    public boolean check(FlowFact fact) {
        boolean matched = false;
        if (prefix == null) {
            matched = executor.match(fact.getString(fieldName), fact.getString(matchField));
        } else {
            List<Map<String, Object>> rows = (List<Map<String, Object>>) fact.getList(prefix);
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    if (executor.match(getString(row, fieldName), getString(row, matchField))) {
                        matched = true;
                        break;
                    }
                }
            }
        }
        return matched;
    }
}
