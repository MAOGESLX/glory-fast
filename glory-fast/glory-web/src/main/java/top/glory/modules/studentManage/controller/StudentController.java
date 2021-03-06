package top.glory.modules.studentManage.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.glory.common.annotation.HandleLog;
import top.glory.common.system.query.QueryGenerator;
import top.glory.common.utils.PageUtils;
import top.glory.common.utils.ResponseResult;
import top.glory.modules.studentManage.service.StudentService;
import top.glory.modules.studentManage.entity.Student;
import top.glory.modules.system.vo.PageInfo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 春秋
 * @Description: 学生管理 Controller
 * @Date: 2020-06-16
 */
@Slf4j
@RequestMapping("/api/studentManage/")
@RestController
public class StudentController {

    @Resource
    private StudentService studentService;

    /**
     * 学生管理-列表查询
     */
    @HandleLog("学生管理-列表查询")
    @RequestMapping(value = "/list")
    public ResponseResult list(@RequestBody Student student, HttpServletRequest req) {
        //组装查询条件
        QueryWrapper<Student> queryWrapper = QueryGenerator.initQueryWrapper(student, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        //组装分页
        IPage<Student> pageList = studentService.page(new Page<Student>(student.getPageNo(), student.getPageSize()), queryWrapper);
        PageInfo pageInfo = PageUtils.transPageData(pageList);
        return ResponseResult.ok(pageInfo);
    }

    /**
     * 学生管理-新增
     */
    @HandleLog("学生管理-新增")
    @PostMapping(value = "/insert")
    public ResponseResult insert(@RequestBody Student student) {
        boolean flag = studentService.save(student);
        if (flag) {
            return ResponseResult.ok("添加成功", student.getId());
        }
        return ResponseResult.fail(500, "添加失败");
    }

    /**
     * 学生管理-修改
     */
    @HandleLog("学生管理-修改")
    @PutMapping(value = "/update")
    public ResponseResult update(@RequestBody Student student) {
        Student studentOld = studentService.getById(student.getId());
        if (studentOld == null) {
            ResponseResult.fail(500, "id找不到");
        } else {
            boolean flag = studentService.updateById(student);
            if (flag) {
                return ResponseResult.ok("修改成功", student.getId());
            }
        }
        return ResponseResult.fail(500, "修改失败");
    }

    /**
     * 学生管理-删除
     */
    @HandleLog("学生管理-删除")
    @DeleteMapping(value = "/delete")
    public ResponseResult delete(@RequestBody List<String> idList) {
        if (idList.size() == 0) {
            ResponseResult.fail(500, "参数错误");
        } else {
            boolean flag = studentService.removeByIds(idList);
            if (flag) {
                return ResponseResult.ok("删除成功，共" + idList.size() + "条");
            }
        }
        return ResponseResult.fail(500, "删除失败");
    }

}
