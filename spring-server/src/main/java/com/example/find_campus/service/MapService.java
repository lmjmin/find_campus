package com.example.find_campus.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.find_campus.dao.IMapDao;
import com.example.find_campus.dto.MapItemDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapService {

    private final IMapDao mapDao;

    private static final Map<String, double[]> CAMPUS_POSITIONS = Map.ofEntries(
            Map.entry("1. 대학본관", new double[] {35.12337821045975, 129.1026809933428}),
            Map.entry("대학본관", new double[] {35.12337821045975, 129.1026809933428}),
            Map.entry("2. 보건복지관", new double[] {35.12266204615481, 129.1029258175948}),
            Map.entry("보건복지관", new double[] {35.12266204615481, 129.1029258175948}),
            Map.entry("3. 건축디자인관", new double[] {35.12415182748975, 129.10211133214523}),
            Map.entry("건축디자인관", new double[] {35.12415182748975, 129.10211133214523}),
            Map.entry("4. 대학원", new double[] {35.122849458973796, 129.10238222518637}),
            Map.entry("대학원", new double[] {35.122849458973796, 129.10238222518637}),
            Map.entry("5. 건축공학실습동", new double[] {35.12477548599242, 129.10161458626692}),
            Map.entry("건축공학실습동", new double[] {35.12477548599242, 129.10161458626692}),
            Map.entry("건축학습동", new double[] {35.12477548599242, 129.10161458626692}),
            Map.entry("6. 학생회관", new double[] {35.12467823956183, 129.10137626495128}),
            Map.entry("학생회관", new double[] {35.12467823956183, 129.10137626495128}),
            Map.entry("7. 동명스타디움", new double[] {35.124317871654185, 129.10124086821125}),
            Map.entry("동명스타디움", new double[] {35.124317871654185, 129.10124086821125}),
            Map.entry("동명스터디움", new double[] {35.124317871654185, 129.10124086821125}),
            Map.entry("8. 학생복지관", new double[] {35.12171815499319, 129.1031812340018}),
            Map.entry("학생복지관", new double[] {35.12171815499319, 129.1031812340018}),
            Map.entry("9. 동명생활관 (2호관)", new double[] {35.121989459867514, 129.10338563661972}),
            Map.entry("동명생활관 (2호관)", new double[] {35.121989459867514, 129.10338563661972}),
            Map.entry("동명생활관(2호관)", new double[] {35.121989459867514, 129.10338563661972}),
            Map.entry("10. 중앙도서관", new double[] {35.120967560709644, 129.1035924257535}),
            Map.entry("중앙도서관", new double[] {35.120967560709644, 129.1035924257535}),
            Map.entry("11. 동명관", new double[] {35.11949073313119, 129.1030115393665}),
            Map.entry("동명관", new double[] {35.11949073313119, 129.1030115393665}),
            Map.entry("12. 반려동물관", new double[] {35.119655070168506, 129.10223979292653}),
            Map.entry("반려동물관", new double[] {35.119655070168506, 129.10223979292653}),
            Map.entry("13. ICT 2관", new double[] {35.11984777380266, 129.10152086924418}),
            Map.entry("ICT 2관", new double[] {35.11984777380266, 129.10152086924418}),
            Map.entry("ICT2관", new double[] {35.11984777380266, 129.10152086924418}),
            Map.entry("14. ICT 3관", new double[] {35.12002793069155, 129.10087565282856}),
            Map.entry("ICT 3관", new double[] {35.12002793069155, 129.10087565282856}),
            Map.entry("ICT3관", new double[] {35.12002793069155, 129.10087565282856}),
            Map.entry("15. ICT 1관", new double[] {35.12013551953265, 129.1032447328397}),
            Map.entry("ICT 1관", new double[] {35.12013551953265, 129.1032447328397}),
            Map.entry("ICT1관", new double[] {35.12013551953265, 129.1032447328397}),
            Map.entry("16. 교수연구동", new double[] {35.120486163453464, 129.1026422885578}),
            Map.entry("교수연구동", new double[] {35.120486163453464, 129.1026422885578}),
            Map.entry("17. 국제대학관", new double[] {35.12050349007318, 129.10177352689968}),
            Map.entry("국제대학관", new double[] {35.12050349007318, 129.10177352689968}),
            Map.entry("18. 동명공업고등학교", new double[] {35.121003077811075, 129.100766342436}),
            Map.entry("동명공업고등학교", new double[] {35.121003077811075, 129.100766342436}),
            Map.entry("19. 국제대학 본관", new double[] {35.12027741458532, 129.1019486876352}),
            Map.entry("국제대학 본관", new double[] {35.12027741458532, 129.1019486876352}),
            Map.entry("Busan International College Office", new double[] {35.12027741458532, 129.1019486876352}),
            Map.entry("20. 쉼터(편의점)", new double[] {35.12065954014353, 129.10212851076417}),
            Map.entry("쉼터(편의점)", new double[] {35.12065954014353, 129.10212851076417}),
            Map.entry("쉼터", new double[] {35.12065954014353, 129.10212851076417}),
            Map.entry("21. 국제산학협력관(학생군사관)", new double[] {35.121015943570036, 129.09963422830856}),
            Map.entry("국제산학협력관(학생군사관)", new double[] {35.121015943570036, 129.09963422830856}),
            Map.entry("국제산학협력관", new double[] {35.121015943570036, 129.09963422830856}),
            Map.entry("22. 가온누리(학생휴게점)", new double[] {35.12200155868467, 129.10242897998995}),
            Map.entry("가온누리(학생휴게점)", new double[] {35.12200155868467, 129.10242897998995}),
            Map.entry("가온누리", new double[] {35.12200155868467, 129.10242897998995}),
            Map.entry("23. 선당", new double[] {35.12019769767011, 129.10394553330747}),
            Map.entry("선당", new double[] {35.12019769767011, 129.10394553330747}),
            Map.entry("24. 창의인재관", new double[] {35.12375187348464, 129.1015306975554}),
            Map.entry("창의인재관", new double[] {35.12375187348464, 129.1015306975554}),
            Map.entry("Do-ing관", new double[] {35.12375187348464, 129.1015306975554}),
            Map.entry("25. 분수대", new double[] {35.12230637424442, 129.10316893988448}),
            Map.entry("26. 용마광장", new double[] {35.12018669289543, 129.1025029756337}),
            Map.entry("용마광장", new double[] {35.12018669289543, 129.1025029756337}),
            Map.entry("27. 제1정문", new double[] {35.12296783333605, 129.10049324582604}),
            Map.entry("제1정문", new double[] {35.12296783333605, 129.10049324582604}),
            Map.entry("28. 제2정문", new double[] {35.12163088160782, 129.09951017166344}),
            Map.entry("제2정문", new double[] {35.12163088160782, 129.09951017166344}),
            Map.entry("29. 제3정문", new double[] {35.120413033884624, 129.10075392950841}),
            Map.entry("제3정문", new double[] {35.120413033884624, 129.10075392950841}),
            Map.entry("30. 제1주차장", new double[] {35.12353718960733, 129.10091918110075}),
            Map.entry("제1주차장", new double[] {35.12353718960733, 129.10091918110075}),
            Map.entry("31. 제2주차장", new double[] {35.122124370782196, 129.10041400069096}),
            Map.entry("제2주차장", new double[] {35.122124370782196, 129.10041400069096}),
            Map.entry("32. 대운동장 1", new double[] {35.121183060785775, 129.10272875416405}),
            Map.entry("대운동장 1", new double[] {35.121183060785775, 129.10272875416405}),
            Map.entry("대운동장1", new double[] {35.121183060785775, 129.10272875416405}),
            Map.entry("33. 대운동장 2", new double[] {35.12209421913468, 129.100983569105}),
            Map.entry("대운동장 2", new double[] {35.12209421913468, 129.100983569105}),
            Map.entry("대운동장2", new double[] {35.12209421913468, 129.100983569105}),
            Map.entry("34. 원형광장", new double[] {35.12325135616879, 129.10207173433406}),
            Map.entry("원형광장", new double[] {35.12325135616879, 129.10207173433406}),
            Map.entry("35. 주차관제소", new double[] {35.122647763008146, 129.10141183864138}),
            Map.entry("주차관제소", new double[] {35.122647763008146, 129.10141183864138}),
            Map.entry("주차관리소", new double[] {35.122647763008146, 129.10141183864138}),
            Map.entry("36. 종합체육시설", new double[] {35.12154128733378, 129.1011695323422}),
            Map.entry("종합체육시설", new double[] {35.12154128733378, 129.1011695323422}),
            Map.entry("37. 제3주차장", new double[] {35.12217796052198, 129.10161090425666}),
            Map.entry("제3주차장", new double[] {35.12217796052198, 129.10161090425666}),
            Map.entry("38. 제5주차장", new double[] {35.11919186666944, 129.10127917874257}),
            Map.entry("제5주차장", new double[] {35.11919186666944, 129.10127917874257}),
            Map.entry("39. 순환버스 정류장", new double[] {35.12156517771354, 129.10174048505553}),
            Map.entry("순환버스 정류장", new double[] {35.12156517771354, 129.10174048505553}),
            Map.entry("순환버스정류장", new double[] {35.12156517771354, 129.10174048505553}),
            Map.entry("40. 마지뜨락", new double[] {35.12222197234009, 129.10011214114272}),
            Map.entry("마지뜨락", new double[] {35.12222197234009, 129.10011214114272}),
            Map.entry("41. 분수대", new double[] {35.12294034887136, 129.10129870579755}),
            Map.entry("분수대", new double[] {35.12294034887136, 129.10129870579755}),
            Map.entry("42. 학생휴게점(cafe)", new double[] {35.122461452951576, 129.10267113385467}),
            Map.entry("학생휴게점(cafe)", new double[] {35.122461452951576, 129.10267113385467}),
            Map.entry("43. 동명생활관 (1호관)", new double[] {35.117042587413465, 129.1063402809453}),
            Map.entry("동명생활관 (1호관)", new double[] {35.117042587413465, 129.1063402809453}),
            Map.entry("동명생활관(1호관)", new double[] {35.117042587413465, 129.1063402809453}),
            Map.entry("44. 동명뜰", new double[] {35.12059237931578, 129.10418052811548}),
            Map.entry("동명뜰", new double[] {35.12059237931578, 129.10418052811548}),
            Map.entry("45. 체육시설관리동", new double[] {35.121337297589676, 129.100980577376}),
            Map.entry("체육시설관리동", new double[] {35.121337297589676, 129.100980577376}),
            Map.entry("46. 그린스타트업타운", new double[] {35.11954635785008, 129.10084409007936}),
            Map.entry("그린스타트업타운", new double[] {35.11954635785008, 129.10084409007936}),
            Map.entry("47. 대학동물병원", new double[] {35.11895422185826, 129.1019914585962}),
            Map.entry("대학동물병원", new double[] {35.11895422185826, 129.1019914585962}),
            Map.entry("대학동명병원", new double[] {35.11895422185826, 129.1019914585962}),
            Map.entry("48. 창업거점지구", new double[] {35.119230010164635, 129.10063854556128}),
            Map.entry("창업거점지구", new double[] {35.119230010164635, 129.10063854556128})
    );

    public List<MapItemDto> findMapItems(String keyword, String filter) {
        String normalizedFilter = filter == null || filter.isBlank() ? "all" : filter;
        List<MapItemDto> items = mapDao.findMapItems(keyword, normalizedFilter);
        items.forEach(this::applyCampusPositionFromLocationName);
        return items;
    }

    private void applyCampusPositionFromLocationName(MapItemDto item) {
        double[] position = findCampusPosition(item.getLocationName());
        if (position == null) {
            return;
        }

        item.setLatitude(position[0]);
        item.setLongitude(position[1]);
    }

    private double[] findCampusPosition(String locationName) {
        if (locationName == null || locationName.isBlank()) {
            return null;
        }

        String normalizedLocationName = locationName.trim();
        double[] exactPosition = CAMPUS_POSITIONS.get(normalizedLocationName);
        if (exactPosition != null) {
            return exactPosition;
        }

        return CAMPUS_POSITIONS.entrySet().stream()
                .filter(entry -> normalizedLocationName.contains(entry.getKey()) || entry.getKey().contains(normalizedLocationName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}