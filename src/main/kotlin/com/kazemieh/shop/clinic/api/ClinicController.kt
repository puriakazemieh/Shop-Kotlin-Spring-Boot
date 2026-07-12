package com.kazemieh.shop.clinic.api

import com.kazemieh.shop.clinic.api.dto.*
import com.kazemieh.shop.clinic.application.ClinicHomeworkService
import com.kazemieh.shop.clinic.application.ClinicMessageService
import com.kazemieh.shop.clinic.application.ClinicService
import com.kazemieh.shop.clinic.application.JournalService
import com.kazemieh.shop.clinic.application.MoodCheckInService
import com.kazemieh.shop.clinic.application.TherapistMatchService
import com.kazemieh.shop.clinic.application.TherapistSwitchService
import com.kazemieh.shop.shared.security.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** بخشِ نیازمندِ احراز هویت: نوبت‌های من، رزرو و لغو، ثبتِ خلق‌وخو، درخواستِ تعویضِ درمانگر، رسیدِ جلسه،
 *  پیام‌رسانی، تکلیف، ژورنال، پرسشنامه‌ی تطبیقِ درمانگر. */
@RestController
@RequestMapping("/api/clinic")
class ClinicController(
    private val clinicService: ClinicService,
    private val moodCheckInService: MoodCheckInService,
    private val therapistSwitchService: TherapistSwitchService,
    private val messageService: ClinicMessageService,
    private val homeworkService: ClinicHomeworkService,
    private val journalService: JournalService,
    private val therapistMatchService: TherapistMatchService
) {

    @GetMapping("/my-appointments")
    fun myAppointments(@AuthenticationPrincipal principal: UserPrincipal): List<AppointmentResponse> =
        clinicService.myAppointments(principal.id)

    @PostMapping("/appointments")
    fun book(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: BookAppointmentRequest
    ): AppointmentResponse = clinicService.book(principal.id, request)

    @PostMapping("/appointments/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ) = clinicService.cancel(principal.id, id)

    @GetMapping("/appointments/{id}/receipt")
    fun receipt(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: Long
    ): SessionReceiptResponse = clinicService.getReceipt(principal.id, id)

    // ---- ثبتِ روزانه‌ی خلق‌وخو ----
    @PostMapping("/mood-checkins")
    fun submitMood(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: MoodCheckInRequest
    ): MoodCheckInResponse = moodCheckInService.submit(principal.id, request.moodScore, request.note)

    @GetMapping("/mood-checkins")
    fun moodHistory(@AuthenticationPrincipal principal: UserPrincipal): List<MoodCheckInResponse> =
        moodCheckInService.history(principal.id)

    // ---- درخواستِ تعویضِ درمانگر ----
    @PostMapping("/switch-requests")
    fun requestSwitch(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: SwitchRequestRequest
    ): SwitchRequestResponse = therapistSwitchService.request(principal.id, request)

    @GetMapping("/switch-requests/mine")
    fun mySwitchRequests(@AuthenticationPrincipal principal: UserPrincipal): List<SwitchRequestResponse> =
        therapistSwitchService.listMine(principal.id)

    // ---- پیام‌رسانیِ امنِ بینِ‌جلسه‌ای ----
    @GetMapping("/therapists/{therapistId}/messages")
    fun listMessages(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable therapistId: Long): List<ClinicMessageResponse> =
        messageService.listThread(therapistId, principal.id)

    @PostMapping("/therapists/{therapistId}/messages")
    fun sendMessage(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable therapistId: Long,
        @RequestBody request: SendMessageRequest
    ): ClinicMessageResponse = messageService.sendAsPatient(principal.id, therapistId, request.body)

    @GetMapping("/therapists/{therapistId}/messaging-status")
    fun messagingStatus(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable therapistId: Long): MessagingPlanStatusResponse =
        messageService.messagingStatus(principal.id, therapistId)

    // ---- تکلیف/تمرینِ بینِ‌جلسه‌ای ----
    @GetMapping("/homework")
    fun myHomework(@AuthenticationPrincipal principal: UserPrincipal): List<HomeworkResponse> =
        homeworkService.listMine(principal.id)

    @PostMapping("/homework/{id}/complete")
    fun completeHomework(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long): HomeworkResponse =
        homeworkService.complete(principal.id, id)

    // ---- یادداشتِ روزانه (ژورنال) ----
    @GetMapping("/journal")
    fun myJournal(@AuthenticationPrincipal principal: UserPrincipal): List<JournalEntryResponse> =
        journalService.listMine(principal.id)

    @PostMapping("/journal")
    fun addJournalEntry(@AuthenticationPrincipal principal: UserPrincipal, @RequestBody request: JournalEntryRequest): JournalEntryResponse =
        journalService.create(principal.id, request)

    @DeleteMapping("/journal/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteJournalEntry(@AuthenticationPrincipal principal: UserPrincipal, @PathVariable id: Long) =
        journalService.delete(principal.id, id)

    // ---- پرسشنامه‌ی تطبیقِ درمانگر ----
    @GetMapping("/therapist-match/questions")
    fun matchQuestions(): List<TherapistMatchQuestionResponse> = therapistMatchService.listQuestions()

    @PostMapping("/therapist-match/submit")
    fun submitMatch(@RequestBody request: SubmitTherapistMatchRequest): List<TherapistMatchResultResponse> =
        therapistMatchService.submitMatch(request)
}
