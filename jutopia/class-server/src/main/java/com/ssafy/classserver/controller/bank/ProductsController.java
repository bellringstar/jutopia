package com.ssafy.classserver.controller.bank;

import com.ssafy.classserver.dto.ClassRoomDto;
import com.ssafy.classserver.dto.ProductDto;
import com.ssafy.classserver.jpa.entity.MemberSavingEntity;
import com.ssafy.classserver.jpa.entity.SavingProductsEntity;
import com.ssafy.classserver.service.BankService;
import com.ssafy.classserver.service.SchoolService;
import com.ssafy.classserver.vo.request.RequestMemberSaving;
import com.ssafy.classserver.vo.request.RequestProduct;
import com.ssafy.classserver.vo.response.ResponseClassRoom;
import com.ssafy.classserver.vo.response.ResponseMemberSaving;
import com.ssafy.classserver.vo.response.ResponseProduct;
import com.ssafy.common.api.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/class-server/api/bank")
@Tag(name = "products", description = "적금 상품 관리 api 입니다.")
public class ProductsController {

    Environment env;
    BankService bankService;
    SchoolService schoolService;
    ModelMapper mapper;

    public ProductsController(Environment env, SchoolService schoolService, BankService bankService) {
        this.env = env;
        this.bankService = bankService;
        this.schoolService = schoolService;
        this.mapper = new ModelMapper();
        this.mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    // 반 적금 상품 생성
    @PostMapping("/{classroomId}/product")
    public Api<ResponseProduct> createProduct(@RequestBody RequestProduct product
                                                        , @PathVariable UUID classroomId) {

        ProductDto productDto = mapper.map(product, ProductDto.class);
        productDto.setClassroomId(classroomId);
        System.out.println("copntroller : " + productDto);
        bankService.createProduct(productDto);
        ResponseProduct responseProduct = mapper.map(productDto, ResponseProduct.class);

        return Api.CREATED(responseProduct);
    }

    // 적금 상품 가져오기
    @GetMapping("/{classroomId}/product")
    public Api<List<ResponseProduct>> getAllProducts(@PathVariable UUID classroomId) {
        Iterable<SavingProductsEntity> plist = bankService.getAllProducts(classroomId);
        List<ResponseProduct> resultList = new ArrayList<>();

        plist.forEach(p -> resultList.add(new ModelMapper().map(p, ResponseProduct.class)));

        return Api.OK(resultList);
    }

    @PostMapping("/product/{memberId}/{productId}")
    public Api<RequestMemberSaving> createMemberSaving(@RequestBody RequestMemberSaving request,
                                                      @PathVariable UUID memberId,
                                                      @PathVariable UUID productId) {
        Optional<SavingProductsEntity> product = bankService.getProduct(productId);

        if (!product.isPresent()) return Api.NOT_FOUND(null);
        MemberSavingEntity memRequest = mapper.map(request, MemberSavingEntity.class);
        memRequest.setSavingProduct(product.get());
        memRequest.setMemberId(memberId);

        MemberSavingEntity result = bankService.createMemProduct(memRequest);
        System.out.println(result);
        return Api.OK(request);
    }

    @GetMapping("/product/{memberId}")
    public Api<ResponseMemberSaving> getMemberSaving(@PathVariable UUID memberId) {
        Optional<MemberSavingEntity> memberSaving = bankService.getMemSaving(memberId);
        if (!memberSaving.isPresent()) return Api.NOT_FOUND(null);

        ResponseMemberSaving res = mapper.map(memberSaving.get(), ResponseMemberSaving.class);
        res.setProductId(memberSaving.get().getSavingProduct().getId());
        res.setProductName(memberSaving.get().getSavingProduct().getProductName());
        res.setProductDetail(memberSaving.get().getSavingProduct().getProductDetail());
        res.setTerm(memberSaving.get().getSavingProduct().getTerm());
        res.setInterestRate(memberSaving.get().getSavingProduct().getInterestRate());

        return Api.OK(res);
    }
}
