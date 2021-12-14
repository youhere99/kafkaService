package com.kafka.service.impl;

import com.kafka.entity.CKStatus;
import com.kafka.mapper.CKStatusMapper;
import com.kafka.service.CKStatusService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service
public class CKStatusServiceImpl extends ServiceImpl<CKStatusMapper, CKStatus> implements CKStatusService {

}
