package kahlua.KahluaProject.service;

import jakarta.transaction.Transactional;
import kahlua.KahluaProject.apipayload.code.status.ErrorStatus;
import kahlua.KahluaProject.converter.ApplyConverter;
import kahlua.KahluaProject.domain.apply.Apply;
import kahlua.KahluaProject.domain.apply.Preference;
import kahlua.KahluaProject.domain.applyInfo.ApplyInfo;
import kahlua.KahluaProject.domain.user.User;
import kahlua.KahluaProject.domain.user.UserType;
import kahlua.KahluaProject.dto.apply.request.ApplyCreateRequest;
import kahlua.KahluaProject.dto.apply.response.*;
import kahlua.KahluaProject.dto.applyInfo.request.ApplyInfoRequest;
import kahlua.KahluaProject.dto.applyInfo.response.ApplyInfoResponse;
import kahlua.KahluaProject.dto.apply.response.ApplyStatisticsResponse;
import kahlua.KahluaProject.exception.GeneralException;
import kahlua.KahluaProject.repository.ApplyInfoRepository;
import kahlua.KahluaProject.repository.ApplyRepository;
import kahlua.KahluaProject.vo.ApplyInfoData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplyService {

    private final ApplyRepository applyRepository;
    private final MailService mailService;
    private final ApplyInfoRepository applyInfoRepository;

    @Transactional
    public ApplyCreateResponse createApply(ApplyCreateRequest applyCreateRequest) {

        if(applyRepository.existsByPhoneNum(applyCreateRequest.getPhone_num())) {
            throw new GeneralException(ErrorStatus.ALREADY_EXIST_APPLICANT);
        }

        Apply apply = ApplyConverter.toApply(applyCreateRequest);
        applyRepository.save(apply);

        mailService.sendApplicantEmail(apply); // 보컬과 나머지 세션으로 구분하여 지원 확인 메일 발송

        ApplyCreateResponse applyCreateResponse = ApplyConverter.toApplyCreateResponse(apply);

        return applyCreateResponse;
    }

    public ApplyGetResponse getApply(Long applyId) {

        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.APPLICANT_NOT_FOUND));

        ApplyGetResponse applyGetResponse = ApplyConverter.toApplyGetResponse(apply);
        return applyGetResponse;
    }

    public ApplyAdminGetResponse getApplyAdmin(Long applyId) {

        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.APPLICANT_NOT_FOUND));

        ApplyAdminGetResponse applyAdminGetResponse = ApplyConverter.toApplyAdminGetResponse(apply);
        return applyAdminGetResponse;
    }

    public ApplyListResponse getApplyList(User user) {

        if(user.getUserType() != UserType.ADMIN){
            throw new GeneralException(ErrorStatus.UNAUTHORIZED);
        }

        List<Apply> applies = applyRepository.findAll();
        List<ApplyItemResponse> applyItemResponses = new ArrayList<>();
        Long total = applyRepository.count();

        for (Apply apply : applies) {
            ApplyItemResponse applyItemResponse = ApplyItemResponse.builder()
                    .id(apply.getId())
                    .name(apply.getName())
                    .phone_num(apply.getPhoneNum())
                    .birth_date(apply.getBirth_date())
                    .gender(apply.getGender())
                    .address(apply.getAddress())
                    .major(apply.getMajor())
                    .first_preference(apply.getFirstPreference())
                    .second_preference(apply.getSecondPreference())
                    .build();

            applyItemResponses.add(applyItemResponse);
        }

        ApplyListResponse applyListResponse = ApplyListResponse.builder()
                .total(total)
                .applies(applyItemResponses)
                .build();

        return applyListResponse;
    }

    public ApplyListResponse getApplyListByPreference(User user, Preference preference) {

        if(user.getUserType() != UserType.ADMIN){
            throw new GeneralException(ErrorStatus.UNAUTHORIZED);
        }

        List<Apply> firstPreferenceApplies = applyRepository.findAllByFirstPreference(preference);
        List<Apply> secondPreferenceApplies = applyRepository.findAllBySecondPreference(preference);

        List<ApplyItemResponse> applyItemResponses = new ArrayList<>();

        for (Apply apply : firstPreferenceApplies) {
            ApplyItemResponse applyItemResponse = ApplyItemResponse.builder()
                    .id(apply.getId())
                    .name(apply.getName())
                    .phone_num(apply.getPhoneNum())
                    .birth_date(apply.getBirth_date())
                    .gender(apply.getGender())
                    .address(apply.getAddress())
                    .major(apply.getMajor())
                    .first_preference(apply.getFirstPreference())
                    .second_preference(apply.getSecondPreference())
                    .build();

            applyItemResponses.add(applyItemResponse);
        }

        for (Apply apply : secondPreferenceApplies) {
            ApplyItemResponse applyItemResponse = ApplyItemResponse.builder()
                    .id(apply.getId())
                    .name(apply.getName())
                    .phone_num(apply.getPhoneNum())
                    .birth_date(apply.getBirth_date())
                    .gender(apply.getGender())
                    .address(apply.getAddress())
                    .major(apply.getMajor())
                    .first_preference(apply.getFirstPreference())
                    .second_preference(apply.getSecondPreference())
                    .build();

            applyItemResponses.add(applyItemResponse);
        }

        ApplyListResponse applyListResponse = ApplyListResponse.builder()
                .total((long)applyItemResponses.size())
                .applies(applyItemResponses)
                .build();

        return applyListResponse;
    }

    public ApplyInfoResponse updateApplyInfo(Long applyInfoId, ApplyInfoRequest applyInfoRequest, User user) {
        //validation: applyId 유효성, 관리자 권한 확인
        ApplyInfo applyInfo = applyInfoRepository.findById(applyInfoId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.APPLY_INFO_NOT_FOUND));

        if(user.getUserType() != UserType.ADMIN){
            throw new GeneralException(ErrorStatus.UNAUTHORIZED);
        }

        //business logic: applyInfo 데이터 변환 및 변경사항 업데이트
        ApplyInfoData applyInfodata = ApplyConverter.toApplyInfo(applyInfoRequest);
        applyInfo.update(applyInfodata);

        applyInfoRepository.save(applyInfo);

        //return: ApplyInfoResponse 타입으로 변환 후 반환
        return ApplyConverter.toApplyInfoResponse(applyInfo);
    }

    public ApplyStatisticsResponse getApplyStatistics(User user) {
        //validation: 관리자 권한 확인
        if (user.getUserType() != UserType.ADMIN) {
            throw new GeneralException(ErrorStatus.UNAUTHORIZED);
        }

        //business logic: 전체 지원자 수, 각 세션별 지원자 수 조회
        Long totalApplyCount = applyRepository.countByDeletedAtIsNull()
                .orElseThrow(() -> new GeneralException(ErrorStatus.APPLICANT_NOT_FOUND));

        Map<Preference, Long> counts = Arrays.stream(Preference.values())
                .collect(Collectors.toMap(
                        preference -> preference,
                        this::getCount
                ));

        //return: ApplyStatisticsResponse 타입으로 변환 후 반환
       return ApplyStatisticsResponse.builder()
                .totalApplyCount(totalApplyCount)
                .vocalCount(counts.get(Preference.VOCAL))
                .vocalPercent(calculatePercent(counts.get(Preference.VOCAL), totalApplyCount))
                .drumCount(counts.get(Preference.DRUM))
                .drumPercent(calculatePercent(counts.get(Preference.DRUM), totalApplyCount))
                .guitarCount(counts.get(Preference.GUITAR))
                .guitarPercent(calculatePercent(counts.get(Preference.GUITAR), totalApplyCount))
                .bassCount(counts.get(Preference.BASS))
                .bassPercent(calculatePercent(counts.get(Preference.BASS), totalApplyCount))
                .synthesizerCount(counts.get(Preference.SYNTHESIZER))
                .synthesizerPercent(calculatePercent(counts.get(Preference.SYNTHESIZER), totalApplyCount))
                .build();
    }

    private Long getCount(Preference preference) {
        return applyRepository.countByFirstPreferenceAndDeletedAtIsNull(preference)
                .orElseThrow(() -> new GeneralException(ErrorStatus.APPLICANT_NOT_FOUND));
    }

    private Long calculatePercent(Long count, Long total) {
        return total > 0 ? (count * 100) / total : 0;
    }

}
