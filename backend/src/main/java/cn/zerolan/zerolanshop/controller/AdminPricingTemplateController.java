package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.common.Result;
import cn.zerolan.zerolanshop.domain.dto.PricingTemplateCreateRequest;
import cn.zerolan.zerolanshop.domain.dto.PricingTemplateResponse;
import cn.zerolan.zerolanshop.domain.dto.PricingTemplateStatusRequest;
import cn.zerolan.zerolanshop.domain.dto.PricingTemplateUpdateRequest;
import cn.zerolan.zerolanshop.service.PricingTemplateService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pricing-templates")
public class AdminPricingTemplateController {

    private final PricingTemplateService pricingTemplateService;

    public AdminPricingTemplateController(PricingTemplateService pricingTemplateService) {
        this.pricingTemplateService = pricingTemplateService;
    }

    /**
     * GET /api/admin/pricing-templates
     * 查询定价模板列表，可按名称、定价方式和状态过滤。
     */
    @GetMapping
    public Result<List<PricingTemplateResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String pricingType,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success(pricingTemplateService.list(name, pricingType, status));
    }

    /**
     * GET /api/admin/pricing-templates/{id}
     * 查询单个定价模板详情。
     */
    @GetMapping("/{id}")
    public Result<PricingTemplateResponse> detail(@PathVariable Long id) {
        return Result.success(pricingTemplateService.detail(id));
    }

    /**
     * POST /api/admin/pricing-templates
     * 创建一个定价模板资源。
     */
    @PostMapping
    public Result<PricingTemplateResponse> create(@RequestBody PricingTemplateCreateRequest request) {
        return Result.success(pricingTemplateService.create(request));
    }

    /**
     * PUT /api/admin/pricing-templates/{id}
     * 更新一个定价模板资源。
     */
    @PutMapping("/{id}")
    public Result<PricingTemplateResponse> update(@PathVariable Long id, @RequestBody PricingTemplateUpdateRequest request) {
        return Result.success(pricingTemplateService.update(id, request));
    }

    /**
     * PATCH /api/admin/pricing-templates/{id}/status
     * 启用或禁用一个定价模板。
     */
    @PatchMapping("/{id}/status")
    public Result<PricingTemplateResponse> updateStatus(@PathVariable Long id, @RequestBody PricingTemplateStatusRequest request) {
        return Result.success(pricingTemplateService.updateStatus(id, request));
    }

    /**
     * DELETE /api/admin/pricing-templates/{id}
     * 逻辑删除一个定价模板。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        pricingTemplateService.delete(id);
        return Result.success();
    }
}
