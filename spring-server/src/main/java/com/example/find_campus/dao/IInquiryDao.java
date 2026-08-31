package com.example.find_campus.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.find_campus.dto.InquiryDto;
import com.example.find_campus.dto.ItemSearchDto;

@Mapper
public interface IInquiryDao {

    int insertInquiry(InquiryDto inquiryDto);

    InquiryDto findInquiryById(@Param("inquiryId") Long inquiryId);

    List<InquiryDto> findInquiries(ItemSearchDto searchDto);

    List<InquiryDto> findInquiriesByUserId(@Param("userId") Long userId);

    int countInquiries(ItemSearchDto searchDto);

    int countInquiriesByStatus(@Param("status") String status);

    int updateInquiryStatus(@Param("inquiryId") Long inquiryId, @Param("status") String status);

    int answerInquiry(@Param("inquiryId") Long inquiryId,
                      @Param("answerContent") String answerContent,
                      @Param("answerAdminId") Long answerAdminId);
}