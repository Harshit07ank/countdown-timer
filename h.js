function startCountdown() {
    var countdownElement = document.getElementById('countdown');
    var countdownDate = new Date().getTime() + 5 * 60 * 1000; // 5 minutes from now

    var countdownInterval = setInterval(function () {
        var now = new Date().getTime();
        var distance = countdownDate - now;

        var hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        var seconds = Math.floor((distance % (1000 * 60)) / 1000);

        var formattedTime = formatTime(hours) + ":" + formatTime(minutes) + ":" + formatTime(seconds);

        countdownElement.textContent = formattedTime;

        if (distance < 0) {
            clearInterval(countdownInterval);
            countdownElement.textContent = "EXPIRED";
        }
    }, 1000);
}

function formatTime(time) {
    return time < 10 ? "0" + time : time;
}
