use ad;

create table if not exists geocoordinates_itemID
(
    itemID        int not null,
    geocoordinate point not null,
    constraint geocoordinates_itemID_pk primary key (itemID), spatial index(geocoordinate)
) ENGINE = MyISAM;

insert into geocoordinates_itemID (itemID, geocoordinate)
select i.item_id, Point(i.latitude, i.longitude)
from item_coordinates i;

CREATE FUNCTION
    get_distance_in_miles_between_geo_locations(
    x1 decimal(10, 6), y1 decimal(10, 6), x2 decimal(10, 6), y2 decimal(10, 6))
    returns decimal(10, 3)
    DETERMINISTIC
BEGIN
    return ((ACOS(SIN(x1 * PI() / 180) * SIN(x2 * PI() / 180) +
                  COS(x1 * PI() / 180) * COS(x2 * PI() / 180) * COS((y1 - y2) * PI() / 180))
                 * 180 / PI()) * 60 * 1.1515);
END;