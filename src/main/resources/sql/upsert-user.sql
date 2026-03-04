INSERT INTO users (id, name, username, email, phone, website,
                  street, suite, city, zipcode, lat, lng,
                  company_name, company_catch_phrase, company_bs)
VALUES (:id, :name, :username, :email, :phone, :website,
        :street, :suite, :city, :zipcode, :lat, :lng,
        :companyName, :companyCatchPhrase, :companyBs)
ON CONFLICT (id) DO UPDATE SET
    name                = EXCLUDED.name,
    username            = EXCLUDED.username,
    email               = EXCLUDED.email,
    phone               = EXCLUDED.phone,
    website             = EXCLUDED.website,
    street              = EXCLUDED.street,
    suite               = EXCLUDED.suite,
    city                = EXCLUDED.city,
    zipcode             = EXCLUDED.zipcode,
    lat                 = EXCLUDED.lat,
    lng                 = EXCLUDED.lng,
    company_name        = EXCLUDED.company_name,
    company_catch_phrase = EXCLUDED.company_catch_phrase,
    company_bs          = EXCLUDED.company_bs

