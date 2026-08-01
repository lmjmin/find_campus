package com.example.find_campus.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.find_campus.dto.FoundItemDto;
import com.example.find_campus.dto.ItemImageDto;
import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.dto.ItemViewDto;
import com.example.find_campus.dto.LostItemDto;
import com.example.find_campus.dto.ReportDto;
import com.example.find_campus.dto.ReportViewDto;

@Mapper
public interface IItemDao {

    int insertLostItem(LostItemDto lostItemDto);

    int insertFoundItem(FoundItemDto foundItemDto);

    int insertReport(ReportDto reportDto);

    int insertItemImage(ItemImageDto itemImageDto);

    List<ItemViewDto> findLostItems(ItemSearchDto searchDto);

    int countLostItems(ItemSearchDto searchDto);

    ItemViewDto findLostItemById(Long lostId);

    int increaseLostViewCount(Long lostId);

    List<ItemViewDto> findFoundItems(ItemSearchDto searchDto);

    int countFoundItems(ItemSearchDto searchDto);

    ItemViewDto findFoundItemById(Long foundId);

    int increaseFoundViewCount(Long foundId);

    List<ItemViewDto> findRecentItems(@Param("itemType") String itemType, @Param("limit") int limit);

    List<ItemViewDto> findLostItemsByUserId(Long userId);

    List<ItemViewDto> findFoundItemsByUserId(Long userId);

    List<ReportViewDto> findReports(ItemSearchDto searchDto);

    int countReports(ItemSearchDto searchDto);

    int updateReportStatus(@Param("reportId") Long reportId, @Param("status") String status);

    int updateLostStatus(@Param("lostId") Long lostId, @Param("status") String status);

    int updateFoundStatus(@Param("foundId") Long foundId, @Param("status") String status);
}
