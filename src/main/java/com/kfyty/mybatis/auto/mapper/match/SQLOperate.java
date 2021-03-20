package com.kfyty.mybatis.auto.mapper.match;

import com.kfyty.mybatis.auto.mapper.handle.strategy.AbstractGenerateMapperLabel;

import java.util.regex.Pattern;

/**
 * 描述: SQL 操作
 *
 * @author kfyty725
 * @date 2021/3/19 17:30
 * @email kfyty725@hotmail.com
 */
public interface SQLOperate {
    /**
     * SQL 操作名称
     * @return 操作名称
     */
    String operate();

    /**
     * mapper 模板
     * @return mapper 模板
     */
    String template();

    /**
     * 匹配 SQL 操作的正则表达式
     * @return 正则表达式
     */
    Pattern pattern();

    /**
     * 生成该 mapper 标签的策略实现类
     * @return 策略实现类
     */
    Class<? extends AbstractGenerateMapperLabel> strategy();
}
