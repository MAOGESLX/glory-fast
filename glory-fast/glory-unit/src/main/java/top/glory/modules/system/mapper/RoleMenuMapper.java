package top.glory.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.glory.modules.system.entity.SysMenu;
import top.glory.modules.system.entity.SysRoleMenu;

/**
 * @Description: 用户管理
 * @Author: 春秋
 * @Date: 2020年4月15日
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<SysRoleMenu> {

    int deleteByRoleId(@Param("roleId") String roleId);
}
