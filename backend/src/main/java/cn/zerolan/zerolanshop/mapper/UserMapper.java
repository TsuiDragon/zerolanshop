package cn.zerolan.zerolanshop.mapper;

import cn.zerolan.zerolanshop.domain.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM `user` WHERE id = #{id} FOR UPDATE")
    User selectByIdForUpdate(Long id);
}
