package top.glory.config.aspect;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.glory.common.annotation.Dict;
import top.glory.common.constant.Constants;
import top.glory.common.utils.ResponseResult;
import top.glory.common.utils.StringUtil;
import top.glory.modules.system.DictService;
import top.glory.modules.system.vo.PageInfo;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 字典aop类
 * @Author: dangzhenghui
 * @Date: 2019-3-17 21:50
 * @Version: 1.0
 */
@Aspect
@Component
@Slf4j
public class DictAspect {
    @Resource
    private DictService dictService;

    // 定义切点Pointcut
    @Pointcut("execution(public * top.glory.modules..*.*Controller.*(..))")
    public void excudeService() {
    }

    @Around("excudeService()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        long time1 = System.currentTimeMillis();
        Object result = pjp.proceed();
        long time2 = System.currentTimeMillis();
        log.debug("获取JSON数据 耗时：" + (time2 - time1) + "ms");
        long start = System.currentTimeMillis();
        this.parseDictText(result);
        long end = System.currentTimeMillis();
        log.debug("解析注入JSON数据  耗时" + (end - start) + "ms");
        return result;
    }

    /**
     * 本方法针对返回对象为Result 的IPage的分页列表数据进行动态字典注入
     * 字典注入实现 通过对实体类添加注解@dict 来标识需要的字典内容,字典分为单字典code即可 ，table字典 code table text配合使用与原来jeecg的用法相同
     * 示例为SysUser   字段为sex 添加了注解@Dict(dicCode = "sex") 会在字典服务立马查出来对应的text 然后在请求list的时候将这个字典text，已字段名称加_dictText形式返回到前端
     * 例输入当前返回值的就会多出一个sex_dictText字段
     * {
     * sex:1,
     * sex_dictText:"男"
     * }
     * 前端直接取值sext_dictText在table里面无需再进行前端的字典转换了
     * customRender:function (text) {
     * if(text==1){
     * return "男";
     * }else if(text==2){
     * return "女";
     * }else{
     * return text;
     * }
     * }
     * 目前vue是这么进行字典渲染到table上的多了就很麻烦了 这个直接在服务端渲染完成前端可以直接用
     *
     * @param result
     */
    private void parseDictText(Object result) {
        if (result instanceof ResponseResult) {
            if (((ResponseResult) result).getResult() instanceof PageInfo) {
                List<Object> items = new ArrayList<>();
                for (Object record : ((PageInfo) ((ResponseResult) result).getResult()).getRecords()) {
                    ObjectMapper mapper = new ObjectMapper();
                    String json = "{}";
                    try {
                        //解决@JsonFormat注解解析不了的问题详见SysAnnouncement类的@JsonFormat
                        json = mapper.writeValueAsString(record);
                    } catch (JsonProcessingException e) {
                        log.error("json解析失败" + e.getMessage(), e);
                    }
                    JSONObject item = JSONObject.parseObject(json);
                    //update-begin--Author:scott -- Date:20190603 ----for：解决继承实体字段无法翻译问题------
                    //for (Field field : record.getClass().getDeclaredFields()) {
                    for (Field field : StringUtil.getAllFields(record)) {
                        //update-end--Author:scott  -- Date:20190603 ----for：解决继承实体字段无法翻译问题------
                        if (field.getAnnotation(Dict.class) != null) {
                            String code = field.getAnnotation(Dict.class).dicCode();
                            String text = field.getAnnotation(Dict.class).dicText();
                            String table = field.getAnnotation(Dict.class).dictTable();
                            String key = String.valueOf(item.get(field.getName()));

                            //翻译字典值对应的txt
                            String textValue = translateDictValue(code, text, table, key);

                            log.debug(" 字典Val : " + textValue);
                            log.debug(" __翻译字典字段__ " + field.getName() + Constants.DICT_TEXT_SUFFIX + "： " + textValue);
                            item.put(field.getName() + Constants.DICT_TEXT_SUFFIX, textValue);
                        }
                        //date类型默认转换string格式化日期
                        if (field.getType().getName().equals("java.util.Date") && field.getAnnotation(JsonFormat.class) == null && item.get(field.getName()) != null) {
                            SimpleDateFormat aDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            item.put(field.getName(), aDate.format(new Date((Long) item.get(field.getName()))));
                        }
                    }
                    items.add(item);
                }
                ((PageInfo) ((ResponseResult) result).getResult()).setRecords(items);
            }

            //处理子表字典表
            if (((ResponseResult) result).getResult() instanceof List) {
                List<JSONObject> items = new ArrayList<>();
                List<String> itemString = new ArrayList<>();
                //                for (Object record : ((List) ((Result) result).getResult())) {
                for (int i = 0; i < ((List) ((ResponseResult) result).getResult()).size(); i++) {
                    Object record = ((List) ((ResponseResult) result).getResult()).get(i);
                    ObjectMapper mapper = new ObjectMapper();
                    String json = "{}";
                    try {
                        //解决@JsonFormat注解解析不了的问题详见SysAnnouncement类的@JsonFormat
                        json = mapper.writeValueAsString(record);
                    } catch (JsonProcessingException e) {
                        log.error("json解析失败" + e.getMessage(), e);
                    }
                    JSONObject item = null;

                    if (json.contains("{")) {
                        item = JSONObject.parseObject(json);
                        //update-begin--Author:scott -- Date:20190603 ----for：解决继承实体字段无法翻译问题------
                        //for (Field field : record.getClass().getDeclaredFields()) {
                        for (Field field : StringUtil.getAllFields(record)) {
                            //update-end--Author:scott  -- Date:20190603 ----for：解决继承实体字段无法翻译问题------
                            if (field.getAnnotation(Dict.class) != null) {
                                String code = field.getAnnotation(Dict.class).dicCode();
                                String text = field.getAnnotation(Dict.class).dicText();
                                String table = field.getAnnotation(Dict.class).dictTable();
                                String key = String.valueOf(item.get(field.getName()));

                                //翻译字典值对应的txt
                                String textValue = translateDictValue(code, text, table, key);

                                log.debug(" 字典Val : " + textValue);
                                log.debug(" __翻译字典字段__ " + field.getName() + Constants.DICT_TEXT_SUFFIX + "： " + textValue);
                                item.put(field.getName() + Constants.DICT_TEXT_SUFFIX, textValue);
                            }
                            //date类型默认转换string格式化日期
                            if (field.getType().getName().equals("java.util.Date") && field.getAnnotation(JsonFormat.class) == null && item.get(field.getName()) != null) {
                                SimpleDateFormat aDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                item.put(field.getName(), aDate.format(new Date((Long) item.get(field.getName()))));
                            }
                        }

                        items.add(item);

                    } else { //处理直接返回string数组的
                        if (json.contains("\"")) {
                            json = json.replace("\"", "");
                        }
                        itemString.add(json);

                    }


                }
                if (items.size() > 0) {
                    ((ResponseResult) result).setResult(items);
                } else {
                    ((ResponseResult) result).setResult(itemString);
                }
            }
        }

    }

    /**
     * 翻译字典文本
     *
     * @param code
     * @param text
     * @param table
     * @param key
     * @return
     */
    private String translateDictValue(String code, String text, String table, String key) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        StringBuffer textValue = new StringBuffer();
        String[] keys = key.split(",");
        for (String k : keys) {
            String tmpValue = null;
            log.debug(" 字典 key : " + k);
            if (k.trim().length() == 0) {
                continue; //跳过循环
            }
            if (!StringUtils.isEmpty(table)) {
                tmpValue = dictService.queryTableDictTextByKey(table, text, code, k.trim());
            } else {
                tmpValue = dictService.queryDictTextByKey(code, k.trim());
            }

            if (tmpValue != null) {
                if (!"".equals(textValue.toString())) {
                    textValue.append(",");
                }
                textValue.append(tmpValue);
            }

        }
        return textValue.toString();
    }

}
