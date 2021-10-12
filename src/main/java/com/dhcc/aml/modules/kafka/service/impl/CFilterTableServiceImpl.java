package com.dhcc.aml.modules.kafka.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhcc.aml.modules.kafka.mapper.CFilterTableMapper;
import com.dhcc.aml.modules.kafka.entity.CFilterTable;
import com.dhcc.aml.modules.kafka.service.CFilterTableService;
/**
  * @author zhaomingxing
  * 描述-
  * date 2021/9/28
  */
@Service
public class CFilterTableServiceImpl extends ServiceImpl<CFilterTableMapper, CFilterTable> implements CFilterTableService{

}
