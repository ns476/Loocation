from imposm.parser import OSMParser

from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy import Column, Integer, String, Boolean, Float

from Geohash import encode as encode_geohash

Base = declarative_base()

class Toilet(Base):
    __tablename__ = "toilets"
    
    id = Column(Integer, primary_key=True)

    name = Column(String)
    is_private = Column(Boolean)
    customers_only = Column(Boolean)

    lat = Column(Float)
    lng = Column(Float)
    
    geohash = Column(String)
    

class ToiletCounter(object):
    def __init__(self, session):
        self.session = session
    def nodes(self, nodes):
        for id, tags, coords in nodes:
            if tags.get("amenity", None) == "toilets" or tags.get("toilets", None) == "yes":
                t = Toilet()
                t.name = tags.get("name", None)
                t.is_private = bool(tags.get("toilets", False))
                t.customers_only = tags.get("toilets:access", "") == "customers"
                
                t.lat = coords[1]
                t.lng = coords[0]

                t.geohash = encode_geohash(t.lat, t.lng)
                self.session.add(t)
def create_database(engine):
    Base.metadata.create_all(engine)

if __name__ == "__main__":
    engine = create_engine('sqlite:///toilets.sqlite', echo=True)
    create_database(engine)

    session = sessionmaker(bind=engine)()
    
    toilets = ToiletCounter(session)
    p = OSMParser(concurrency=2, nodes_callback=toilets.nodes)
    p.parse("london.osm.pbf")
    session.commit()
