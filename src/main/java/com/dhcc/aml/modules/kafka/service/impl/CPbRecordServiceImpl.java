package com.dhcc.aml.modules.kafka.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dhcc.aml.modules.kafka.entity.CPbRecord;
import com.dhcc.aml.modules.kafka.mapper.CPbRecordMapper;
import com.dhcc.aml.modules.kafka.service.CPbRecordService;
/**
  * @author zhaomingxing
  * 描述-
  * date 2021/9/23
  */
@Service
public class CPbRecordServiceImpl extends ServiceImpl<CPbRecordMapper, CPbRecord> implements CPbRecordService{

}
