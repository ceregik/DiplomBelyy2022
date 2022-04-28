package App.data.booking.get;

import java.util.List;

public class GetBookingsResponse {

    private List<Booking> bookings;

    public GetBookingsResponse(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}
