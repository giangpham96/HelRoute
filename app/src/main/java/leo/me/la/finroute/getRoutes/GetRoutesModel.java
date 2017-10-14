package leo.me.la.finroute.getRoutes;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx.RxApollo;

import leo.me.la.finroute.RouteQuery;
import leo.me.la.finroute.type.InputCoordinates;
import rx.Observable;
import rx.functions.Func1;

class GetRoutesModel implements GetRoutesMVP.Model {

    private ApolloClient client;
    private ApolloCall<RouteQuery.Data> call;

    GetRoutesModel(ApolloClient client) {
        this.client = client;
    }


    @Override
    public Observable<RouteQuery.Itinerary> result(String transportMode, int maxTrans, double walkingReluctance, double walkingSpeed, InputCoordinates from, InputCoordinates to, boolean isArriveBy, String date, String time) {
        RouteQuery query = RouteQuery.builder()
                .from(from).to(to)
                .date(date).time(time)
                .walkReluctance(walkingReluctance)
                .walkSpeed(walkingSpeed)
                .maxTransfers(maxTrans)
                .arriveBy(isArriveBy)
                .modes(transportMode)
                .build();
        call = client.query(query);
        return RxApollo.from(call).concatMap(new Func1<Response<RouteQuery.Data>, Observable<? extends RouteQuery.Itinerary>>() {
            @Override
            public Observable<? extends RouteQuery.Itinerary> call(Response<RouteQuery.Data> dataResponse) {
                if (dataResponse.data() != null && dataResponse.data().plan() != null)
                    return Observable.from(dataResponse.data().plan().itineraries());
                return null;
            }
        });
    }

    @Override
    public void close() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }
}
