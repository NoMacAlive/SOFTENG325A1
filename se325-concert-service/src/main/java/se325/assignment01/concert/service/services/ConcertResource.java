package se325.assignment01.concert.service.services;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.assignment01.concert.common.dto.*;
import se325.assignment01.concert.common.types.BookingStatus;
import se325.assignment01.concert.service.domain.*;
import se325.assignment01.concert.service.jaxrs.LocalDateTimeParam;
import se325.assignment01.concert.service.mapper.*;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.awt.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@Path("/concert-service/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConcertResource {

    private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);
    private final static Set<Subscriber> subs = new HashSet<>();


    /**
     * GET a concert by id.
     * if no concert with that id
     * 404 not found is returned
     * @param id
     * @return
     */
    @GET
    @Path("concerts/{id}")
    public Response retrieveConcert(@PathParam("id") long id) {

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            // Start a transaction for persisting the audit data.
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c where c.id = :id", Concert.class)
                    .setParameter("id", id);
            Concert concert = concertQuery.getSingleResult();
            Response.ResponseBuilder builder = Response.ok(ConcertMapper.toDto(concert));
            return builder.build();
        }catch(NoResultException e){
                // Return a HTTP 404 response if the specified Concert isn't found
            return Response.status(404).build();

        } finally {
            em.close();
        }
    }

    /**
     *
     * @param concertDTO
     * @return
     */
    @POST
    @Path("concerts/")
    public Response createConcert(ConcertDTO concertDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(ConcertMapper.toDomainModel(concertDTO));
            em.getTransaction().commit();

            Response.ResponseBuilder builder = Response.created(URI.create("/concerts/" + concertDTO.getId()));
            return builder.build();
        } finally {
            em.close();
        }
    }

    /**
     * retrive all concerts in the database
     * generic types are wrapped in genericentities
     * @return
     */
    @GET
    @Path("concerts/")
    public Response retrieveAllConcerts() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();

            if(concerts.isEmpty()){
                return Response.noContent().build();
            }

            List<ConcertDTO> concertDTOS = new ArrayList<>();
            for(Concert concert:concerts){
                concertDTOS.add(ConcertMapper.toDto(concert));
            }
            GenericEntity<List<ConcertDTO>> genericConcerts = new GenericEntity<List<ConcertDTO>>(concertDTOS){};
            return Response.ok(genericConcerts).build();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            em.close();
        }
        return Response.noContent().build();
    }


    @PUT
    @Path("concerts/")
    public Response updateConcert(Concert concert) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(concert);
            em.getTransaction().commit();

            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } finally {
            em.close();
        }
    }

    @DELETE
    @Path("concerts/{id}")
    public Response delete(@PathParam("id") long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            Concert concert = em.find(Concert.class, id);

            if (concert == null) {
                // Return a HTTP 404 response if the specified Concert isn't found.
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            em.remove(concert);
            em.getTransaction().commit();

            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } finally {
            em.close();
        }
    }


    @DELETE
    @Path("concerts/")
    public Response deleteAllConcerts() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();

            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();

            for (Concert c : concerts) {
                em.remove(c);
            }

            em.getTransaction().commit();

            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } finally {
            em.close();
        }
    }

    /**
     * authenticate users represented by the userDTO class.
     * if user is successfully logged in a uniquelly generated token is returned
     * @param userDTO
     * @return
     */
    @POST
    @Path("login")
    public Response authenticateUser(UserDTO userDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            if (userDTO.getUsername() == null || userDTO.getPassword() == null ||
                    userDTO.getUsername().isEmpty() || userDTO.getPassword().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            System.out.println(userDTO.getUsername());
            try {
                Query userQuery = em.createQuery("select u from User u where u.userName= :orgName");
                userQuery.setParameter("orgName", userDTO.getUsername());
                User user = (User) userQuery.getSingleResult();
                if (!user.getPassWord().equals(userDTO.getPassword())) {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
                //generate a token and sent it in auth cookie
                String token = UUID.randomUUID().toString();
                user.setToken(token);
                em.persist(user);
                em.getTransaction().commit();
                return Response.ok()
                        .entity(UserMapper.toDTO(user))
                        .cookie(new NewCookie("auth", token))
                        .build();
            }catch(NoResultException e1){
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    /**
     * return all the concertSummaries
     * @return
     */
    @GET
    @Path("concerts/summaries")
    public Response getConcertSummary(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();

            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();

            List<ConcertSummaryDTO> concertDTOS = new ArrayList<>();
            for(Concert concert:concerts){
                concertDTOS.add(ConcertSummaryMapper.toDto(concert));
            }
            GenericEntity<List<ConcertSummaryDTO>> genericConcerts = new GenericEntity<List<ConcertSummaryDTO>>(concertDTOS){};
            return Response.ok(genericConcerts).build();
        }catch(Exception e){
            e.printStackTrace();
            return Response.serverError().build();
        }finally {
            em.close();
        }
    }

    /**
     * Get booking by id
     * only the booker the booking can access it
     * otherwise HTTP FORBIDDEN is returned
     * @param id
     * @param token
     * @return
     */
    @GET
    @Path("bookings/{id}")
    public Response getBooking(@PathParam("id") long id,@CookieParam("auth") Cookie token){
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            TypedQuery<Booking> bookingQ = em.createQuery("select b from Booking b where id = :id",Booking.class).setParameter("id",id);
            Booking booking = bookingQ.getSingleResult();

            if(!booking.getUser().getToken().equals(token.getValue())){
                return Response.status(Response.Status.FORBIDDEN.getStatusCode()).build();
            }

            return Response.ok(BookingMapper.toDTO(booking)).build();

        }catch(NoResultException e){
            e.printStackTrace();
            return Response.status(404).build();
        }finally {
            em.close();
        }

    }


    /**
     * Return all bookings for the current logged in user
     * @param token
     * @return
     */
    @GET
    @Path("bookings")
    public Response getAllBookingsForUser(@CookieParam("auth") Cookie token) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        List<BookingDTO> bookingDTO = new ArrayList<>();
        try {
            if(token == null){
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }

            em.getTransaction().begin();
            TypedQuery<User> que= em.createQuery("select u from User u where u.token = :tok",User.class).setParameter("tok",token.getValue());
            User user = que.getSingleResult();

            TypedQuery<Booking> qu= em.createQuery("select u from Booking u "+
                                                        "inner join u.user b " +
                                                        "where b.id = :i",
                                                        Booking.class).setParameter("i",user.getId());
            List<Booking> bookings = qu.getResultList();



            for(Booking booking:bookings){
                System.out.println(booking.getId());
                bookingDTO.add(BookingMapper.toDTO(booking));
            }
            GenericEntity genericBookings = new GenericEntity<List<BookingDTO>>(bookingDTO){};
            return Response.ok(genericBookings).build();
        }catch(NoResultException e){
            GenericEntity genericBookingDTO = new GenericEntity<List<BookingDTO>>(bookingDTO){};
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        }finally {
            em.close();
        }
    }


    /**
     * Create new booking
     * querying for seats are on optimistic lock mode to avoid concurrent modification
     * successfull booking will be notified to those who subscibed to corresponding concerts and dates
     * @param u
     * @param uriInfo
     * @param token
     * @return
     */
    @POST
    @Path("bookings")
    public Response bookASeat(BookingRequestDTO u, @Context UriInfo uriInfo,@CookieParam("auth") Cookie token){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        URI uri = uriInfo.getAbsolutePath();
        Booking booking;
        try {
            if (token == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            em.getTransaction().begin();
            //get concert
            Concert concert = em.find(Concert.class,u.getConcertId());
            if (concert == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            //get localdatetime
            LocalDateTime local = u.getDate();
            if (local == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            //get Seats list
            List<String> seatsLabel = u.getSeatLabels();
            List<Seat> seats;
            List<Seat> seat = new ArrayList<>();
            TypedQuery<Seat> q = em.createQuery("select s from Seat s where s.label in :seatLabels and s.date = :date", Seat.class)
                    .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                    .setParameter("seatLabels", u.getSeatLabels())
                    .setParameter("date", u.getDate());

            seats = q.getResultList();
            if(seats.isEmpty()){
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            boolean allSeatsAvailable = true;
            for(Seat s:seats){
                if(s.isBooked()){
                    allSeatsAvailable = false;
                    break;
                }
                if(seatsLabel.contains(s.getLabel())){
                    seat.add(s); //all the seats with labels at the local date time is added to the list
                    s.setBooked(true);
                }
            }
            //get user
            User user;
            TypedQuery<User> que= em.createQuery("select u from User u where u.token = :tok",User.class).setParameter("tok",token.getValue());
            user = que.getSingleResult();

            if(allSeatsAvailable) {
                for (Seat s : seat) {
                    em.merge(s);
                }
                //create new booking for those seat
                booking = new Booking(concert, local, seat, user);

                em.persist(booking);
            }else{
                return Response.status(Response.Status.FORBIDDEN.getStatusCode()).build();
            }
            em.getTransaction().commit();

        }catch(Exception e){
            if(e.getMessage().equals("403")){
                return Response.status(Response.Status.FORBIDDEN.getStatusCode()).build();
            }else if(e.getMessage().equals("401")){
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            return Response.serverError().build();
        }finally {
            em.close();
        }
        publish(u.getConcertId(),u.getDate());
        return  Response.created(URI.create(uri+"/"+ booking.getId().toString())).build();
    }


    @POST
    @Path("subscribe/concertInfo")
    public Response subscribe(@Suspended AsyncResponse sub, @CookieParam("auth") Cookie cookie, ConcertInfoSubscriptionDTO u){

        EntityManager em = PersistenceManager.instance().createEntityManager();

        try{
            em.getTransaction().begin();
        //if unautherised
            if(cookie==null){
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }else{
                TypedQuery<User> userq = em.createQuery("select s from User s where s.token = :d",User.class).setParameter("d",cookie.getValue());
                User user = userq.getSingleResult();
            }

            //none exist date or concert
            LocalDateTime date = u.getDate();
            long concertID = u.getConcertId();
            Concert concert = em.find(Concert.class,concertID);
            if(concert == null||!concert.getDates().contains(date)){
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            //otherwise...
            synchronized (subs){
                subs.add(new Subscriber(concertID,sub,date,u.getPercentageBooked()));
            }

            return Response.ok().build();
        }catch(NoResultException e){
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }finally {
            em.close();
        }

    }

    public void publish(long concertId, LocalDateTime date){
        synchronized(subs){
            for(Subscriber sub:subs){
                if(sub.getConcertId()==concertId) {
                    long remainingSeatsPercentage = calculateForRemainingSeatsPercentage(concertId,date);
                    if(remainingSeatsPercentage == -1){
                        break;
                    }
                    if(remainingSeatsPercentage<=sub.getPercentage()) {
                        sub.getSub().resume(new ConcertInfoNotificationDTO((int)(120 * (remainingSeatsPercentage)/100)));
                    }
                }
            }
            subs.clear();
        }
    }

    @GET
    @Path("seats/{date}")
    public Response getAllSeatsForDate(@PathParam("date") LocalDateTimeParam dateParam, @QueryParam("status") BookingStatus status){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            Query seatsQuery = em.createQuery("select s from Seat s where s.date = :d");
            seatsQuery.setParameter("d",dateParam.getLocalDateTime());
            List<Seat> seats = seatsQuery.getResultList();

            if(seats.isEmpty()){
                return Response.noContent().build();
            }
            List<SeatDTO> dtoseats = new ArrayList<>();
            if(status.equals(BookingStatus.Any)) {
                for (Seat s : seats) {
                    dtoseats.add(SeatMapper.toDto(s));
                }
            }else if(status.equals(BookingStatus.Booked)){
                for (Seat s : seats) {
                    if(s.isBooked()) {
                        dtoseats.add(SeatMapper.toDto(s));
                    }
                }
            }else if(status.equals(BookingStatus.Unbooked)){
                for (Seat s : seats) {
                    if(!s.isBooked()) {
                        dtoseats.add(SeatMapper.toDto(s));
                    }
                }
            }
            GenericEntity<List<SeatDTO>> genericSeats = new GenericEntity<List<SeatDTO>>(dtoseats){};
            return Response.ok(genericSeats).build();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            em.close();
        }

        return Response.noContent().build();
    }

    @GET
    @Path("performers/{id}")
    public Response getPerformer(@PathParam("id") long id){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            Query performerQuery = em.createQuery("select s from Performer s where s.id = :d");
            performerQuery.setParameter("d", id);
            Performer per = (Performer) performerQuery.getSingleResult();
            return Response.ok(PerformerMapper.toDTO(per)).build();
        }catch(NoResultException ne){
            return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
        }catch(Exception e){
//            e.printStackTrace();
            return Response.serverError().build();
        }finally {
            em.close();
        }
    }

    @GET
    @Path("performers/")
    public Response getPerformers(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            TypedQuery<Performer> performerQuery = em.createQuery("select s from Performer s",Performer.class);
            List<Performer> performers = performerQuery.getResultList();
            List<PerformerDTO> performerDTOS = new ArrayList<>();
            for(Performer p:performers){
                performerDTOS.add(PerformerMapper.toDTO(p));
            }
            return Response.ok(performerDTOS).build();
        }catch(Exception e){
            e.printStackTrace();
            return Response.serverError().build();
        }finally {
            em.close();
        }
    }


    private long calculateForRemainingSeatsPercentage(long concertID,LocalDateTime date){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        long remainingSeats = -1;
        try{
            em.getTransaction().begin();

            Concert concert = em.find(Concert.class, concertID);


            if (concert == null||!concert.getDates().contains(date)){
                return -1;
            }

            TypedQuery<Seat> unbookedQ = em.createQuery("select s from Seat s where s.date = :concertDate and s.isBooked = :bookingStatus", Seat.class)
                    .setParameter("concertDate", date)
                    .setParameter("bookingStatus", false);

            TypedQuery<Seat> bookedQ = em.createQuery("select s from Seat s where s.date = :concertDate and s.isBooked = :bookingStatus", Seat.class)
                    .setParameter("concertDate", date)
                    .setParameter("bookingStatus", true);

            remainingSeats = (unbookedQ.getResultList().size() *100)/(unbookedQ.getResultList().size() + bookedQ.getResultList().size());

            em.getTransaction().commit();
        }
        finally {
            em.close();
        }

        return remainingSeats;

    }
}
