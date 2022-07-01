import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import com.kafka.entity.CExLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.junit.Test;

import java.util.Map;

/**
 * @author zhaomingxing
 * 描述-
 * date 2021/12/15
 */
@Slf4j
public class TestEscape {

    @Test
    public void testE() {
        String sql = "insert into YWUSER_PRPCITEMDEVICE (ITEMNO,CLIENTNO,CLIENTTEL,MAKEYEAR,MAKEDATE,POLICYNO,FLAG,CAROWNER,FUELNAME,DEVICENO,USEDATE,USAGE,RISKCODE,REALUSEPRESSURE,REALUSETEMPERATURE,REMARK,MAKEFACTORY,CAPACITY,PREUSETEMPERATURE,PREUSEPRESSURE,DEVICENAME,MODEL,CLIENTNAME,OP_TS,OP_TYPE) values ('0',null,null,null,null,'6211816282230637840',null,'符进','LRDS6PEBXGR01533C','欧曼牌BJ4L67'MB-A',null,'5','2230',null,null,null,'货车',null,null,null,null,'湘N0J031',null,'2021-12-09 11:03:54.000099','I')";
        sql = StringEscapeUtils.escapeSql(sql);
        log.info(sql);
        sql = "欧曼牌BJ4L67'MB-A";
        sql = StringEscapeUtils.escapeSql(sql);
        log.info(sql);
    }

    /**
     * 获取类的属性名
     */
    @Test
    public void testLambda() {
        /**
         * 获取tablefiled字段名
         */
        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(CExLog.class);
        getColumn(CExLog::getTableName, columnMap);
    }

    private String getColumn(SFunction<CExLog, ?> column, Map<String, ColumnCache> columnMap) {

        SerializedLambda lambda = LambdaUtils.resolve(column);
        //类的属性名
        String fieldName = PropertyNamer.methodToProperty(lambda.getImplMethodName());
        ColumnCache columnCache = columnMap.get(LambdaUtils.formatKey(fieldName));
        return columnCache.getColumn();
    }



}
