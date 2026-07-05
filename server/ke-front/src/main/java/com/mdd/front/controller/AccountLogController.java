package com.mdd.front.controller;

import com.mdd.common.aop.NotLogin;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.validator.annotation.IDMust;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.IArticleService;
import com.mdd.front.service.IUserAccountLogService;
import com.mdd.front.validate.article.ArticleCollectValidate;
import com.mdd.front.validate.article.ArticleSearchValidate;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.validate.users.UserAccountLogSearchValidate;
import com.mdd.front.vo.article.ArticleCateVo;
import com.mdd.front.vo.article.ArticleCollectVo;
import com.mdd.front.vo.article.ArticleDetailVo;
import com.mdd.front.vo.article.ArticleListedVo;
import com.mdd.front.vo.user.UserAccountListVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/account_log")
@Api(tags = "用户资金变更管理")
public class AccountLogController {

    @Resource
    IUserAccountLogService iUserAccountLogService;

    @GetMapping("/lists")
    @ApiOperation(value="用户资金变更列表")
    public AjaxResult<PageResult<UserAccountListVo>> lists(@Validated PageValidate pageValidate,
                                                           @Validated UserAccountLogSearchValidate searchValidate) {
        searchValidate.setUserId(LikeFrontThreadLocal.getUserId());
        PageResult<UserAccountListVo> list = iUserAccountLogService.lists(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }
}
