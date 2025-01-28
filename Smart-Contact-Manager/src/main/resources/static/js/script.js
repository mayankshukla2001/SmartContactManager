console.log("hiiiii");
$(document).ready(function() {
    // Toggle sidebar visibility when hamburger icon (fa-bars) is clicked
    $(".fa-bars").click(function() {
        $(".sidebar").toggleClass("show-sidebar"); // Toggle the 'show-sidebar' class
    });

    // Hide sidebar when the close button (crossBtn) is clicked
    $(".crossBtn").click(function() {
        $(".sidebar").removeClass("show-sidebar"); // Remove the 'show-sidebar' class
    });
});

