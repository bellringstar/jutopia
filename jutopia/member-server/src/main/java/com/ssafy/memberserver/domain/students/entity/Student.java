package com.ssafy.memberserver.domain.students.entity;

import com.ssafy.memberserver.common.enums.MemberBioStatus;
import com.ssafy.memberserver.common.enums.MemberRole;
import com.ssafy.memberserver.common.enums.MemberStatus;
import com.ssafy.memberserver.common.enums.SeatOwnershipStatus;
import com.ssafy.memberserver.common.error.ErrorCode;
import com.ssafy.memberserver.common.exception.ApiException;
import com.ssafy.memberserver.domain.pointtransaction.dto.request.PointIncomeRequest;
import com.ssafy.memberserver.domain.pointtransaction.dto.request.PointExpenseRequest;
import com.ssafy.memberserver.domain.students.dto.request.StudentDeleteRequest;
import com.ssafy.memberserver.domain.students.dto.request.StudentPointUpdateRequest;
import com.ssafy.memberserver.domain.students.dto.request.StudentUpdateRequest;
import com.ssafy.memberserver.domain.students.sign.dto.signUp.StudentSignUpRequest;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Slf4j
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String studentId;
    private String studentPwd;
    private String studentName;
    private BigDecimal point;
    private BigDecimal money;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    MemberBioStatus memberBioStatus;
    @Enumerated(EnumType.STRING)
    MemberRole memberRole;
    @Enumerated(EnumType.STRING)
    MemberStatus memberStatus;
    @Enumerated(EnumType.STRING)
    SeatOwnershipStatus seatOwnershipStatus;
    private String school;
    private Integer grade;
    private Integer classRoom;
    private Integer studentNumber;

    public static Student from(StudentSignUpRequest studentSignUpRequest, PasswordEncoder passwordEncoder){
        return  Student.builder()
                .studentId(studentSignUpRequest.getStudentId())
                .studentPwd(passwordEncoder.encode(studentSignUpRequest.getStudentPwd()))
                .studentName(studentSignUpRequest.getStudentName())
                .point(studentSignUpRequest.getPoint())
                .money(studentSignUpRequest.getMoney())
                .memberBioStatus(MemberBioStatus.INACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(studentSignUpRequest.getUpdateTimeAt())
                .memberRole(studentSignUpRequest.getMemberRole())
                .memberStatus(studentSignUpRequest.getMemberStatus())
                .school(studentSignUpRequest.getSchool())
                .grade(studentSignUpRequest.getGrade())
                .classRoom(studentSignUpRequest.getClassRoom())
                .studentNumber(studentSignUpRequest.getStudentNumber())
                .seatOwnershipStatus(studentSignUpRequest.getSeatOwnershipStatus())
                .build();
    }
    public void update(StudentUpdateRequest studentUpdateRequest, PasswordEncoder passwordEncoder){
        if(studentUpdateRequest.getStudentNewPwd() != null || !studentUpdateRequest.getStudentPwd().isBlank()){
            this.studentPwd = passwordEncoder.encode(studentUpdateRequest.getStudentNewPwd());
        }
    }
    public void delete(StudentDeleteRequest studentDeleteRequest){
        if(studentDeleteRequest.getMemberStatus() == MemberStatus.ACTIVE){
            this.memberStatus = MemberStatus.INACTIVE;
        }
    }
    public void pointUpdate(StudentPointUpdateRequest studentPointUpdateRequest){
        BigDecimal temp = this.point.subtract(studentPointUpdateRequest.getPoint());
        if(temp.compareTo(BigDecimal.ZERO) >= 0) {
            this.point = point.subtract(studentPointUpdateRequest.getPoint());
        }else{
            throw new ApiException(ErrorCode.STUDENT_POINT_ERROR,"포인트가 부족합니다.");
        }
    }
    public void pointIncomeUpdate(PointIncomeRequest pointIncomeRequest, BigDecimal addPoint){
        if(pointIncomeRequest.income() != null){
            this.point = addPoint;
        }
    }
    public void pointExpenseUpdate(PointExpenseRequest pointExpenseRequest, BigDecimal subtractPoint){
        if(pointExpenseRequest.expense() != null){
            this.point = subtractPoint;
        }
    }
}
