package com.ctrip.infosec.flowtable4j.payAdapt;

import com.ctrip.infosec.flowtable4j.model.FlowFact;
import com.ctrip.infosec.flowtable4j.model.RiskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 流量规则按 订单类型、支付类型区分
 * Created by thyang on 2015/3/13 0013.
 */
public abstract class BaseRule {

    final Logger logger = LoggerFactory.getLogger(BaseRule.class);

    /**
     * 订单类型区分
     */
    private HashMap<Integer, HashMap<String, List<PayAdaptStatement>>> byOrderType = new HashMap<Integer, HashMap<String, List<PayAdaptStatement>>>();


    /**
     * 黑白名单校验不同逻辑
     *
     * @param fact
     * @param results
     * @return
     */
    public abstract boolean check(FlowFact fact, RiskResult results);

    /**
     * 按订单类型校验
     * @param byOrderType
     * @param fact
     * @param results
     * @return
     */
    public abstract boolean checkByOrderType(HashMap<Integer, HashMap<String, List<PayAdaptStatement>>> byOrderType, FlowFact fact, RiskResult results);


    /**
     * 优先校验按订单类型区分的规则
     *
     * @param fact
     * @param results
     * @return
     */
    protected boolean checkByOrderTypeMap(FlowFact fact, RiskResult results) {
        try {
            checkByOrderType(byOrderType, fact, results);
        } catch (Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * 新增规则，流量规则适用全量替换
     *
     * @param rules
     * @return
     */
    public boolean addRule(List<PayAdaptStatement> rules) {

        if (rules != null && rules.size() > 0) {
            HashMap<Integer, HashMap<String, List<PayAdaptStatement>>> orderTypeMapTemp = new HashMap<Integer, HashMap<String, List<PayAdaptStatement>>>();
            buildRuleTree(rules, orderTypeMapTemp);
            byOrderType = orderTypeMapTemp;
        }
        return true;
    }

    /**
     * 构造规则树，按订单类型、支付类型
     */
    private void buildRuleTree(List<PayAdaptStatement> rules, HashMap<Integer, HashMap<String, List<PayAdaptStatement>>> orderTypeMapTemp) {
        Integer orderType = 0;
        String prepayType = "";
        for (PayAdaptStatement rule : rules) {
            orderType = rule.getOrderType();
            prepayType = rule.getPrepayType();
            HashMap<String, List<PayAdaptStatement>> orderTypeRule = null;
            if (orderTypeMapTemp.containsKey(orderType)) {
                orderTypeRule = orderTypeMapTemp.get(orderType);
            } else {
                orderTypeRule = new HashMap<String, List<PayAdaptStatement>>();
            }
            addRuleByPrepayType(orderTypeRule, prepayType, rule);
            orderTypeMapTemp.put(orderType, orderTypeRule);
        }
    }

    private void addRuleByPrepayType(HashMap<String, List<PayAdaptStatement>> orderTypeRule, String prepayType, PayAdaptStatement rule) {
        List<PayAdaptStatement> prepayRules;
        if (orderTypeRule.containsKey(prepayType)) {
            prepayRules = orderTypeRule.get(prepayType);
        } else {
            prepayRules = new ArrayList<PayAdaptStatement>();
        }
        if (!prepayRules.contains(rule)) {
            prepayRules.add(rule);
        }
        orderTypeRule.put(prepayType, prepayRules);
    }
}