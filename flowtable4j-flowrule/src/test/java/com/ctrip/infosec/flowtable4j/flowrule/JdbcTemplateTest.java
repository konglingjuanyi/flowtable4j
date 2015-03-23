/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ctrip.infosec.flowtable4j.flowrule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.Resource;
import javax.sql.DataSource;

import com.ctrip.infosec.flowtable4j.flowrule.entity.FlowRuleEntity;
import com.ctrip.infosec.flowtable4j.flowrule.entity.RuleMatchFieldEntity;
import com.ctrip.infosec.flowtable4j.flowrule.entity.RuleStatisticEntity;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author zhengby
 */
public class JdbcTemplateTest {

    @Resource(name = "cardRiskDBTemplate")
    JdbcTemplate cardRiskDBTemplate;
    @Resource(name = "riskCtrlPreProcDBTemplate")
    JdbcTemplate riskCtrlPreProcDBTemplate;

    @Test
    public void testQueryCardRiskDB() {
        System.out.println("CardRiskDB");
        List<Map<String,Object>> results = cardRiskDBTemplate.queryForList("select top 100* from dbo.InfoSecurity_FlowRule");
        System.out.println("results: " + results.size());
    }

    @Test
    public void testQueryRiskCtrlPreProcDB() {
        System.out.println("RiskCtrlPreProcDB");
        List<Map> results = riskCtrlPreProcDBTemplate.queryForList("select top 100 * from CTRIP_Car_CCardNoCode_Amount", Map.class);
        System.out.println("results: " + results.size());
    }

    @Test
    @Ignore
    public void testQueryBW(){
//        int size = 10;
//        System.out.println(">>>print BWList begin...");
//        List<Map<String,Object>> results = CardRiskDB.queryForList("select r.RuleID,r.OrderType,c.CheckName,c.CheckType,cv.CheckValue \n" +
//                "from dbo.CardRisk_BlackListRule r inner join dbo.CardRisk_BlackListRuleColumnValue cv \n" +
//                "on r.RuleID= cv.RuleID \n" +
//                "Inner join dbo.CardRisk_BlackListColumn c on cv.ProcessType=c.ProcessType \n" +
//                "Where r.Active='T' \n" +
//                "Order by r.OrderType,r.RuleID,c.CheckType");
//        for(int i=0;i<size;i++){
//            for(Iterator it=results.get(i).keySet().iterator();it.hasNext();){
//                Object key = it.next();
//                System.out.print(key + ":" + results.get(i).get(key)+" ");
//            }
//            System.out.println();
//        }
//        System.out.println("<<<print BWList end...");
    }

    /**
     * 比较listMatch 和 listStatistic的top1 的entity的FlowRuleID
     * 若listMatch的较小（<=）则将listMatch 的top1 add 进入 结果集（如果有相同FlowRuleID不覆盖）
     * 反之类似
     */
    @Test
    public void ruleFull(){
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl("jdbc:sqlserver://devdb.dev.sh.ctriptravel.com:28747;database=CardRiskDB;integratedSecurity=false");
        dataSource.setUsername("uws_AllInOneKey_dev");
        dataSource.setPassword("!QAZ@WSX1qaz2wsx");
        dataSource.setMaxIdle(5);
        dataSource.setMaxActive(50);
        JdbcTemplate cardRiskDB = new JdbcTemplate(dataSource);

        List<Map<String,Object>> listMatch = cardRiskDB.queryForList(
                "select f.FlowRuleID,f.RuleName,f.RiskLevel,f.OrderType,f.PrepayType,f.RuleDesc,d.ColumnName,r.MatchType,r.MatchValue,d.TableName\n" +
                        "   From dbo.InfoSecurity_RuleMatchField r\n" +
                        "   join dbo.Def_RuleMatchField d on r.FieldID= d.FieldID\n" +
                        "   join dbo.InfoSecurity_FlowRule f on f.FlowRuleID=r.FlowRuleID WHERE f.Active='T' ORDER BY f.FlowRuleID");
        List<Map<String,Object>> listStatistic = cardRiskDB.queryForList("select f.FlowRuleID,f.RuleName,f.RiskLevel,f.OrderType,f.PrepayType,f.RuleDesc,d.ColumnName as KeyColumnName,\n" +
                "d1.ColumnName as MatchColumnName,d2.StatisticTableName as KeyTableName,r.MatchType,r.MatchValue,r.StatisticType,r.StartTimeLimit,r.TimeLimit,r.SqlValue\n" +
                "   From  dbo.InfoSecurity_RuleStatistic r \n" +
                "         join dbo.Def_RuleMatchField d on r.KeyFieldID=d.FieldID\n" +
                "         join dbo.Def_RuleMatchField d1 on r.MatchFieldID=d1.FieldID\n" +
                "         join dbo.Def_RuleStatisticTable d2 on r.StatisticTableID=d2.StatisticTableID\n" +
                "         join dbo.InfoSecurity_FlowRule f on r.FlowRuleID=f.FlowRuleID WHERE f.Active='T' ORDER BY f.FlowRuleID");

        List<FlowRuleEntity> ruleList = new ArrayList<FlowRuleEntity>();

        int count = listMatch.size()+listStatistic.size();
        int p_match = 0;
        int p_statistic=0;
        boolean matchIsOver = false;
        boolean statisticIsOver = false;
        //f.FlowRuleID,f.RuleName,f.RiskLevel,f.OrderType,f.PrepayType,f.RuleDesc,d.ColumnName,r.MatchType,r.MatchValue,d.TableName

        for(int i=0;i<count;i++){
            if(p_match>=listMatch.size()){
                p_match = listMatch.size()-1;
                matchIsOver = true;
            }
            if(p_statistic>=listStatistic.size()){
                p_statistic = listStatistic.size()-1;
                statisticIsOver = true;
            }
            int matchID = Integer.valueOf(listMatch.get(p_match).get("FlowRuleID").toString());
            int staticID = Integer.valueOf(listStatistic.get(p_statistic).get("FlowRuleID").toString());
//            FlowRuleEntity flowRuleEntity = ruleList.get(ruleList.size() - 1);
            if(statisticIsOver||matchID<=staticID&&p_match<listMatch.size()&&!matchIsOver){

                if(ruleList.size()==0||matchID!=ruleList.get(ruleList.size() - 1).getFlowRuleID()){
                    FlowRuleEntity flowRuleEntity12Add = new FlowRuleEntity();
                    ruleList.add(flowRuleEntity12Add);
                    flowRuleEntity12Add.setFlowRuleID(Integer.valueOf( listMatch.get(p_match).get("FlowRuleID").toString()));
                    flowRuleEntity12Add.setRuleName(listMatch.get(p_match).get("RuleName").toString());
                    flowRuleEntity12Add.setRiskLevel(Integer.valueOf( listMatch.get(p_match).get("RiskLevel").toString()));
                    flowRuleEntity12Add.setOrderType(Integer.valueOf(listMatch.get(p_match).get("OrderType").toString()));
                    flowRuleEntity12Add.setPrepayType(listMatch.get(p_match).get("PrepayType").toString());
                    flowRuleEntity12Add.setRuleDesc(listMatch.get(p_match).get("RuleDesc").toString());

                    RuleMatchFieldEntity ruleMatchFieldEntity = new RuleMatchFieldEntity();
                    List<RuleMatchFieldEntity> list = flowRuleEntity12Add.getMatchFieldListItem();
                    if(list==null){
                        list=new ArrayList<RuleMatchFieldEntity>();
                    }
                    list.add(ruleMatchFieldEntity);
                    flowRuleEntity12Add.setMatchFieldListItem(list);
                    ruleMatchFieldEntity.setColumnName(listMatch.get(p_match).get("ColumnName").toString());
                    ruleMatchFieldEntity.setMatchType(listMatch.get(p_match).get("MatchType").toString());
                    ruleMatchFieldEntity.setMatchValue(listMatch.get(p_match).get("MatchValue").toString());
                    ruleMatchFieldEntity.setTableName(listMatch.get(p_match).get("TableName").toString());

                }else{
                    RuleMatchFieldEntity ruleMatchFieldEntity = new RuleMatchFieldEntity();
                    ruleMatchFieldEntity.setColumnName(listMatch.get(p_match).get("ColumnName").toString());
                    ruleMatchFieldEntity.setMatchType(listMatch.get(p_match).get("MatchType").toString());
                    ruleMatchFieldEntity.setMatchValue(listMatch.get(p_match).get("MatchValue").toString());
                    ruleMatchFieldEntity.setTableName(listMatch.get(p_match).get("TableName").toString());
//                    List<RuleMatchFieldEntity> list = ruleList.get(ruleList.size() - 1).getMatchFieldListItem();
//                    if(list==null){
//                        list = new ArrayList<RuleMatchFieldEntity>();
//                    }
//                    list.add(ruleMatchFieldEntity);

                    if(ruleList.get(ruleList.size() - 1).getMatchFieldListItem()==null){
                        ruleList.get(ruleList.size() - 1).setMatchFieldListItem(new ArrayList<RuleMatchFieldEntity>());
                    }
                    ruleList.get(ruleList.size() - 1).getMatchFieldListItem().add(ruleMatchFieldEntity);
                }
                p_match++;
            }
            if(matchIsOver||matchID>staticID&&p_statistic<listStatistic.size()&&!statisticIsOver) {

                if(ruleList.size()==0||staticID!=ruleList.get(ruleList.size() - 1).getFlowRuleID()){

                    FlowRuleEntity flowRuleEntity12Add = new FlowRuleEntity();
                    ruleList.add(flowRuleEntity12Add);
                    flowRuleEntity12Add.setFlowRuleID(Integer.valueOf( listMatch.get(p_match).get("FlowRuleID").toString()));
                    flowRuleEntity12Add.setRuleName(listMatch.get(p_match).get("RuleName").toString());
                    flowRuleEntity12Add.setRiskLevel(Integer.valueOf( listMatch.get(p_match).get("RiskLevel").toString()));
                    flowRuleEntity12Add.setOrderType(Integer.valueOf(listMatch.get(p_match).get("OrderType").toString()));
                    flowRuleEntity12Add.setPrepayType(listMatch.get(p_match).get("PrepayType").toString());
                    flowRuleEntity12Add.setRuleDesc(listMatch.get(p_match).get("RuleDesc").toString());

                    RuleStatisticEntity ruleStatisticEntity = new RuleStatisticEntity();

                    List<RuleStatisticEntity> list = flowRuleEntity12Add.getStatisticListItem();
                    if(list==null){
                        list = new ArrayList<RuleStatisticEntity>();
                    }
                    list.add(ruleStatisticEntity);
                    flowRuleEntity12Add.setStatisticListItem(list);
                    ruleStatisticEntity.setMatchValue(listStatistic.get(p_statistic).get("MatchValue").toString());
                    ruleStatisticEntity.setMatchType(listStatistic.get(p_statistic).get("MatchType").toString());
                    ruleStatisticEntity.setKeyColumnName(listStatistic.get(p_statistic).get("KeyColumnName").toString());
                    ruleStatisticEntity.setKeyTableName(listStatistic.get(p_statistic).get("KeyTableName").toString());
                    ruleStatisticEntity.setMatchColumnName(listStatistic.get(p_statistic).get("MatchColumnName").toString());
//                    ruleStatisticEntity.setMatchTableName(listStatistic.get(p_statistic).get("MatchTableName").toString());
                    ruleStatisticEntity.setSqlValue(listStatistic.get(p_statistic).get("SqlValue").toString());
                    ruleStatisticEntity.setStartTimeLimit(Integer.valueOf(listStatistic.get(p_statistic).get("StartTimeLimit").toString()));
                    ruleStatisticEntity.setStatisticType(listStatistic.get(p_statistic).get("StatisticType").toString());
                    ruleStatisticEntity.setTimeLimit(Integer.valueOf(listStatistic.get(p_statistic).get("TimeLimit").toString()));
                }else{

                    RuleStatisticEntity ruleStatisticEntity = new RuleStatisticEntity();
                    ruleStatisticEntity.setMatchValue(listStatistic.get(p_statistic).get("MatchValue").toString());
                    ruleStatisticEntity.setMatchType(listStatistic.get(p_statistic).get("MatchType").toString());
                    ruleStatisticEntity.setKeyColumnName(listStatistic.get(p_statistic).get("KeyColumnName").toString());
                    ruleStatisticEntity.setKeyTableName(listStatistic.get(p_statistic).get("KeyTableName").toString());
                    ruleStatisticEntity.setMatchColumnName(listStatistic.get(p_statistic).get("MatchColumnName").toString());
//                    ruleStatisticEntity.setMatchTableName(listStatistic.get(p_statistic).get("MatchTableName").toString());
                    ruleStatisticEntity.setSqlValue(listStatistic.get(p_statistic).get("SqlValue").toString());
                    ruleStatisticEntity.setStartTimeLimit(Integer.valueOf(listStatistic.get(p_statistic).get("StartTimeLimit").toString()));
                    ruleStatisticEntity.setStatisticType(listStatistic.get(p_statistic).get("StatisticType").toString());
                    ruleStatisticEntity.setTimeLimit(Integer.valueOf(listStatistic.get(p_statistic).get("TimeLimit").toString()));
//                    List<RuleStatisticEntity> list = ruleList.get(ruleList.size() - 1).getStatisticListItem();
//                    if(list==null){
//                        list = new ArrayList<RuleStatisticEntity>();
////                        ruleList.get(ruleList.size() - 1).setStatisticListItem(new ArrayList<RuleStatisticEntity>());
//                    }
//                    list.add(ruleStatisticEntity);
//
                    if(ruleList.get(ruleList.size() - 1).getStatisticListItem()==null){
                        ruleList.get(ruleList.size() - 1).setStatisticListItem(new ArrayList<RuleStatisticEntity>());
                    }
                    ruleList.get(ruleList.size() - 1).getStatisticListItem().add(ruleStatisticEntity);
                }
                p_statistic++;
            }

        }

        RuleManager.SetRuleEntities(ruleList);
    }
}