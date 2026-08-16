# SubscriptionManager
Created By-
Raiyan Choudhury - NSU CSE Dept. 
Tafsir Hasan - NSU CSE Dept.
Hassan Shayer Palok - NSU CSE Dept.

Admin Panel -
| Field         | Value      |
| ------------- | ---------- |
| **Username**  | `admin`    |
| **Password**  | `admin123` |
| **Safe Pass** | `Admin1`   |

|default Subscription datas used |

writeSubscriptionLine(writer, "Windows 11", "Software", "Monthly", 20.0, "20/08/2026",
                "windowstafsir123@gmail.com", "windowstafsir123", true, true, "Visa",
                "4532 1122 8890 4586", "20/07/2026", "09:00", "https://www.microsoft.com/");
        writeSubscriptionLine(writer, "Adobe Creative Cloud", "Software", "Annual", 240.0, "15/12/2026",
                "adobetafsir234@gmail.com", "adobetafsir123", true, true, "MasterCard",
                "5412 7734 9081 7865", "15/12/2025", "11:30", "https://www.adobe.com/creativecloud.html");
        writeSubscriptionLine(writer, "Microsoft 365", "Software", "Annual", 120.0, "10/10/2026",
                "microsofttafsir345@gmail.com", "microsofttafsir123", true, true, "Visa",
                "4485 6621 3390 0122", "10/10/2025", "08:45", "https://www.microsoft.com/microsoft-365");
        writeSubscriptionLine(writer, "Netflix", "Streaming", "Monthly", 15.0, "25/08/2026",
                "netflixtafsir567@gmail.com", "nettafsir123", true, true, "Visa",
                "4716 2290 5581 4466", "25/07/2026", "19:00", "https://www.netflix.com/");
        writeSubscriptionLine(writer, "Spotify", "Streaming", "Monthly", 11.0, "15/09/2026",
                "spotifytafsir56@gmail.com", "spottafsir123", true, true, "MasterCard",
                "5588 3341 2207 7878", "15/08/2026", "17:20", "https://www.spotify.com/");
        writeSubscriptionLine(writer, "Disney+", "Streaming", "Annual", 120.0, "01/01/2027",
                "disneytafsir09@gmail.com", "disneytafsir123", true, true, "Visa",
                "4024 0071 6650 1243", "01/01/2026", "12:00", "https://www.disneyplus.com/");
        writeSubscriptionLine(writer, "Gold Gym", "Gym", "Monthly", 30.0, "30/08/2026",
                "goldgymtafsir67@gmail.com", "goldtafsir123", true, true, "MasterCard",
                "5299 4471 8823 0989", "30/07/2026", "07:00", "https://www.goldsgym.com/");
        writeSubscriptionLine(writer, "FitZone", "Gym", "Annual", 300.0, "01/11/2026",
                "fitzone11tafsir@gmail.com", "fittafsir123", true, true, "Visa",
                "4147 8832 1190 7712", "01/11/2025", "16:00", "https://www.fitzone.com/");

        // Expired subscriptions - renewal dates are before 16/08/2026.
        writeSubscriptionLine(writer, "X-box Game Pass", "Streaming", "Monthly", 15.0, "08/12/2025",
                "xboxgamepass@gmail.com", "pass123", false, false, "Visa",
                "4532 1122 8890 1234", "08/11/2025", "10:00", "https://www.xbox.com/xbox-game-pass");
        writeSubscriptionLine(writer, "Amazon Prime Watch", "Streaming", "Monthly", 8.99, "08/12/2025",
                "amazonwatch@gmail.com", "watch123", false, false, "Gift Card",
                "GC-9981-2234", "08/11/2025", "10:00", "https://www.amazon.com/prime");



 writeSubscriptionLine(writer, "Canva", "Software", "Monthly", 12.0, "07/09/2026",
                    "canvapalok123@gmail.com", "canvapalok12", true, true, "Visa",
                    "1234567890123456", "07/08/2026", "10:00", "https://www.canva.com/");
            writeSubscriptionLine(writer, "Hulu", "Streaming", "Monthly", 7.99, "10/09/2026",
                    "hulupalok@gmail.com", "hulu123", true, true, "Gift Card",
                    "GC-7766-1122", "10/08/2026", "08:00", "https://www.hulu.com/");


 saveCredentials("tafsir", "tafsir123", "01700000000", "Tafsir");
            saveCredentials("palok", "palok123@", "01800000000", "Palok1");
            saveCredentials("admin", "admin123", "01900000000", "Admin1");

Windows 11,Software,Monthly,20.0,20/08/2026,windowstafsir123@gmail.com,Ynx7cXpiZmF0c2Z8ZyQnJg==,true,true,Visa,4532 1122 8890 4586,20/07/2026,09:00,https://www.microsoft.com/
Adobe Creative Cloud,Software,Annual,240.0,15/12/2026,adobetafsir234@gmail.com,dHF6d3BhdHNmfGckJyY=,true,true,MasterCard,5412 7734 9081 7865,15/12/2025,11:30,https://www.adobe.com/creativecloud.html
Microsoft 365,Software,Annual,120.0,10/10/2026,microsofttafsir345@gmail.com,eHx2Z3pmenNhYXRzZnxnJCcm,true,true,Visa,4485 6621 3390 0122,10/10/2025,08:45,https://www.microsoft.com/microsoft-365
Netflix,Streaming,Monthly,15.0,25/08/2026,netflixtafsir567@gmail.com,e3BhYXRzZnxnJCcm,true,true,Visa,4716 2290 5581 4466,25/07/2026,19:00,https://www.netflix.com/
Spotify,Streaming,Monthly,11.0,15/09/2026,spotifytafsir56@gmail.com,ZmV6YWF0c2Z8ZyQnJg==,true,true,MasterCard,5588 3341 2207 7878,15/08/2026,17:20,https://www.spotify.com/
Disney+,Streaming,Annual,120.0,01/01/2027,disneytafsir09@gmail.com,cXxme3BsYXRzZnxnJCcm,true,true,Visa,4024 0071 6650 1243,01/01/2026,12:00,https://www.disneyplus.com/
Gold Gym,Gym,Monthly,30.0,30/08/2026,goldgymtafsir67@gmail.com,cnp5cWF0c2Z8ZyQnJg==,true,true,MasterCard,5299 4471 8823 0989,30/07/2026,07:00,https://www.goldsgym.com/
FitZone,Gym,Annual,300.0,01/11/2026,fitzone11tafsir@gmail.com,c3xhYXRzZnxnJCcm,true,true,Visa,4147 8832 1190 7712,01/11/2025,16:00,https://www.fitzone.com/
X-box Game Pass,Streaming,Monthly,15.0,08/12/2025,xboxgamepass@gmail.com,ZXRmZiQnJg==,false,false,Visa,4532 1122 8890 1234,08/11/2025,10:00,https://www.xbox.com/xbox-game-pass
Amazon Prime Watch,Streaming,Monthly,8.99,08/12/2025,amazonwatch@gmail.com,YnRhdn0kJyY=,false,false,Gift Card,GC-9981-2234,08/11/2025,10:00,https://www.amazon.com/prime
Hoichoi,Streaming,Monthly,20.0,16/09/2025,raiyanc01@gmail.com,JCcmISQnJiE=,true,true,Visa,1234123412341234,16/08/2025,00:46,https://github.com/Raiyanc01/SubscriptionManager.git
