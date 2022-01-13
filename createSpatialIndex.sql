create table if not exists geocoordinates_itemID
(
    itemID        int not null,
    geocoordinate point not null,
    constraint geocoordinates_itemID_pk primary key (itemID)
) ENGINE = MyISAM;

insert into geocoordinates_itemID (itemID, geocoordinate)
select i.item_id, Point(i.longitude, i.latitude)
from item_coordinates i;

create spatial index if not exists geocoordinate_index on geocoordinates_itemID (geocoordinate);