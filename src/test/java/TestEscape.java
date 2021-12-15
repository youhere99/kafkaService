import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

/**
 * @author zhaomingxing
 * 描述-
 * date 2021/12/15
 */
@Slf4j
public class TestEscape {

    @Test
    public void testE(){
        String sql="insert into YWUSER_PRPCITEMDEVICE (ITEMNO,CLIENTNO,CLIENTTEL,MAKEYEAR,MAKEDATE,POLICYNO,FLAG,CAROWNER,FUELNAME,DEVICENO,USEDATE,USAGE,RISKCODE,REALUSEPRESSURE,REALUSETEMPERATURE,REMARK,MAKEFACTORY,CAPACITY,PREUSETEMPERATURE,PREUSEPRESSURE,DEVICENAME,MODEL,CLIENTNAME,OP_TS,OP_TYPE) values ('0',null,null,null,null,'6211816282230637840',null,'符进','LRDS6PEBXGR01533C','欧曼牌BJ4L67'MB-A',null,'5','2230',null,null,null,'货车',null,null,null,null,'湘N0J031',null,'2021-12-09 11:03:54.000099','I')";
        sql = StringEscapeUtils.escapeSql(sql);
        log.info(sql);
        sql="欧曼牌BJ4L67'MB-A";
        sql = StringEscapeUtils.escapeSql(sql);
        log.info(sql);
    }
}
