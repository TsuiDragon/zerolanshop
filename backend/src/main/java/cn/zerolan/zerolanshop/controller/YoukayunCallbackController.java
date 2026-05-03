package cn.zerolan.zerolanshop.controller;

import cn.zerolan.zerolanshop.service.VirtualOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/callbacks/youkayun")
public class YoukayunCallbackController {

    private final VirtualOrderService virtualOrderService;

    public YoukayunCallbackController(VirtualOrderService virtualOrderService) {
        this.virtualOrderService = virtualOrderService;
    }

    @PostMapping("/order")
    public String order(@RequestParam Map<String, String> params) {
        virtualOrderService.handleYoukayunCallback(params);
        return "OK";
    }
}
