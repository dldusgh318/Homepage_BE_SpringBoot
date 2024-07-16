package kahlua.KahluaProject.converter;

import kahlua.KahluaProject.domain.ticket.Participants;
import kahlua.KahluaProject.domain.ticket.Ticket;
import kahlua.KahluaProject.dto.request.ParticipantsCreateRequest;
import kahlua.KahluaProject.dto.request.TicketCreateRequest;
import kahlua.KahluaProject.dto.response.ParticipantsResponse;
import kahlua.KahluaProject.dto.response.TicketCreateResponse;
import kahlua.KahluaProject.dto.response.TicketGetResponse;
import kahlua.KahluaProject.repository.ParticipantsRepository;
import kahlua.KahluaProject.repository.TicketRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

//@Component
public class TicketConverter {

    public static Ticket toTicket(TicketCreateRequest ticketCreateRequest, String reservationId) {
        Ticket ticket;
        ticket = Ticket.builder()
                .buyer(ticketCreateRequest.getBuyer())
                .phone_num(ticketCreateRequest.getPhone_num())
                .reservationId(reservationId)
                .type(ticketCreateRequest.getType())
                .major(ticketCreateRequest.getMajor())
                .studentId(ticketCreateRequest.getStudentId())
                .meeting(ticketCreateRequest.getMeeting())
                .build();

        return ticket;
    }

    public static TicketCreateResponse toTicketCreateResponse(Ticket ticket, String reservationId, List<Participants> members) {
        return TicketCreateResponse.builder()
                .id(ticket.getId())
                .buyer(ticket.getBuyer())
                .phone_num(ticket.getPhone_num())
                .reservationId(reservationId)
                .type(ticket.getType())
                .major(ticket.getMajor())
                .studentId(ticket.getStudentId())
                .meeting(ticket.getMeeting())
                .members(members)
                .status(ticket.getStatus())
                .build();
    }

    public static TicketGetResponse toTicketGetResponse(Ticket ticket, List<Participants> participants) {
        List<ParticipantsResponse> memberResponses = participants.stream()
                .map(member -> ParticipantsResponse.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .phone_num(member.getPhone_num())
                        .build())
                .collect(Collectors.toList());

        return TicketGetResponse.builder()
                .id(ticket.getId())
                .buyer(ticket.getBuyer())
                .phone_num(ticket.getPhone_num())
                .reservationId(ticket.getReservationId())
                .type(ticket.getType())
                .major(ticket.getMajor())
                .studentId(ticket.getStudentId())
                .meeting(ticket.getMeeting())
                .members(memberResponses)
                .status(ticket.getStatus())
                .build();
    }
}
