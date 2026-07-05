package com.mdd.front.service.k;

import com.mdd.common.core.PageResult;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.k.StarInstagramCreateValidate;
import com.mdd.front.validate.k.StarInstagramSearchValidate;
import com.mdd.front.validate.k.StarInstagramUpdateValidate;
import com.mdd.front.vo.k.StarInstagramDetailVo;
import com.mdd.front.vo.k.StarInstagramListedVo;

/**
 * 明星照片墙服务接口类
 * @author LikeAdmin
 */
public interface IStarInstagramService {

    /**
     * 明星照片墙列表
     *
     * @author LikeAdmin
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<StarInstagramListedVo>
     */
    PageResult<StarInstagramListedVo> list(PageValidate pageValidate, StarInstagramSearchValidate searchValidate);

    /**
     * 明星照片墙详情
     *
     * @author LikeAdmin
     * @param id 主键ID
     * @return StarInstagramDetailVo
     */
    StarInstagramDetailVo detail(Integer id);

    /**
     * 明星照片墙新增
     *
     * @author LikeAdmin
     * @param createValidate 参数
     */
    void add(StarInstagramCreateValidate createValidate);

    /**
     * 明星照片墙编辑
     *
     * @author LikeAdmin
     * @param updateValidate 参数
     */
    void edit(StarInstagramUpdateValidate updateValidate);

    /**
     * 明星照片墙删除
     *
     * @author LikeAdmin
     * @param id 主键ID
     */
    void del(String id);

    /**
     * 检查用户是否有权限下载指定图片
     *
     * @param userid 用户的ID，用于标识请求下载的用户
     * @param imageName 需要检查的图片名称
     * @return 返回一个布尔值，表示用户是否有权限下载该图片
     *         true表示有权限，false表示无权限
     */
    boolean checkDownload(Integer userid, String imageName);

    int getDownloadCount(Integer userid);
}
