package top.glory.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;
import top.glory.common.annotation.Dict;
import top.glory.common.entity.DataEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @author 春秋 2020-04-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysUser extends DataEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private String id;

    /**
     * 登录账号
     */
    @Excel(name = "账号", width = 15,orderNum = "10")
    private String loginName;

    /**
     * 真实姓名
     */
    @Excel(name = "真实姓名", width = 15,orderNum = "20")
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * md5密码盐
     */
    private String salt;

    /**
     * 头像
     */
    private String photo;

    /**
     * 生日
     */
    @Excel(name = "生日", width = 15, format = "yyyy-MM-dd",orderNum = "30")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 性别(0-默认未知，1-男，2-女)
     */
    @Excel(name = "性别", width = 15,dicCode="sex",orderNum = "40")
    @Dict(dicCode = "sex")
    private String sex;

    /**
     * 电子邮箱
     */
    @Excel(name = "电子邮箱", width = 15,orderNum = "50")
    private String email;

    /**
     * 电话
     */
    @Excel(name = "电话", width = 15,orderNum = "60")
    private String phone;

    /**
     * 机构编码
     */
    private String orgCode;

    /**
     * 用户状态(1-正常，2-禁用)
     */
    @Dict(dicCode = "user_status")
    private String status;


    /**
     * 员工编号，唯一键
     */
    @Excel(name = "员工编号", width = 15,orderNum = "1")
    private String empNo;

    /**
     * 手机
     */
    @Excel(name = "手机", width = 15,orderNum = "65")
    private String telephone;


    /**
     * 删除时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deleteTime;

    @TableField(exist = false)
    private String[] roles;

}